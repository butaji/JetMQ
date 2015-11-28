package net.jetmq.infra

import java.net.InetSocketAddress

import akka.actor.Actor
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import net.jetmq.broker.Packet
import play.api.libs.json._

import scala.concurrent.duration._


class LogstashTcpConnection(remote: InetSocketAddress) extends Actor {

  implicit val disp = context.system.dispatcher

  val manager = IO(Tcp)(context.system)
  manager ! Connect(remote)

  def receive = {
    case CommandFailed(x: Connect) => {
      println("[Error] LogstashTcpConnection: connect failed " + x)

      println("[Info] LogstashTcpConnection: Reconnecting...")
      context.system.scheduler.scheduleOnce(1.second) {
        manager ! Connect(remote)
      }
    }

    case c@Connected(remote, local) =>
      println("[Info] LogstashTcpConnection: connected to " + remote)
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: PacketTrace =>
          implicit val fmt1 = PacketFormat
          implicit val fmt2 = Json.format[PacketTrace]

          val s: String = Json.toJson(data).toString() + "\n"

          connection ! Write(ByteString(s))

        case data: LogstashMessage =>
          implicit val fmt2 = Json.format[LogstashMessage]

          val s: String = Json.toJson(data).toString() + "\n"

          connection ! Write(ByteString(s))

        case CommandFailed(w: Write) =>
          // O/S buffer was full
          println("[Warning] LogstashTcpConnection: Write failed")
        case Received(data) =>
          println("[Info] received " + data)
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          println("[Info] LogstashTcpConnection: Connection closed")

          println("[Info] LogstashTcpConnection: Reconnecting...")

          context.system.scheduler.scheduleOnce(1.second) {
            manager ! Connect(remote)
          }

      }
  }
}

case class LogstashMessage(level: String, source: String, log_class: String, message: String, cause: Option[String] = None)

case class PacketTrace(source: String, in: Boolean, packet: Packet)

object PacketFormat extends Format[Packet] {

  def writes(p: Packet) = JsObject(Seq(

    "type" -> JsString(p.getClass.getSimpleName),
    "dup" -> JsBoolean(p.header.dup),
    "qos" -> JsNumber(p.header.qos),
    "retain" -> JsBoolean(p.header.retain)
  ))

  override def reads(json: JsValue): JsResult[Packet] = ???
}




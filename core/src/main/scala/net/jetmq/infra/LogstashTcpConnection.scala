package net.jetmq.infra

import java.net.InetSocketAddress

import akka.io.Tcp.Received
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.scaladsl.{Sink, Source, Tcp}
import akka.util.ByteString
import net.jetmq.broker.{ActorPublisherWithBuffer, Packet}
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global


class LogstashTcpConnection(remote: InetSocketAddress) extends ActorPublisherWithBuffer[ByteString] {

  implicit val materializer = ActorMaterializer()(context.system)

    val connection = Tcp(context.system).outgoingConnection(remote)

    val source = Source(ActorPublisher[ByteString](self))

    source
      .map(x => x)
      .via(connection)
      .runWith(Sink.ignore)
      .onComplete(x => {
        print("[Warn] LogstashTcpConnection sending completed. Closing")
        context.stop(self)
      })

  def receive = {

    case data: PacketTrace => {
      implicit val fmt1 = PacketFormat
      implicit val fmt2 = Json.format[PacketTrace]
      val s: String = Json.toJson(data).toString() + "\n"
      onNextBuffered(ByteString(s))
    }

    case data: LogstashMessage => {
      implicit val fmt2 = Json.format[LogstashMessage]
      val s: String = Json.toJson(data).toString() + "\n"
      onNextBuffered(ByteString(s))
    }

    case Received(data) => {
      println("[Info] LogstashTcpConnection received " + data)
    }

    case Request(count) => {
      println("[Info] LogstashTcpConnection Requested: " + count + " demand is " + totalDemand)
      deliverBuffer()
    }

    case Cancel => {
      println("[Info] LogstashTcpConnection was canceled")
      context.stop(self)
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

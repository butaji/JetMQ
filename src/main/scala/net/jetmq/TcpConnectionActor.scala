package net.jetmq.broker

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import net.jetmq.Helpers._
import net.jetmq.packets.Packet

case class ReceivedPacket(packet: Packet)

case class SendingPacket(packet: Packet)

case object Closing

class TcpConnectionActor(sessions: ActorRef) extends Actor with ActorLogging {

  val mqtt = context.actorOf(Props(new MqttConnectionActor(sessions)))

  def receive = {
    case Tcp.Received(data) => {

      val packets = PacketsHelper.decode(data.toBitVector)

      packets.foreach(x => x match {
        case Left(p: Packet) => mqtt ! ReceivedPacket(p)
        case Right(p) => {
          log.warning("" + p)
          sender ! Tcp.Close
        }
      })

      context become receive(sender)
    }

  }


  def receive(connection: ActorRef):Receive = {

    case Tcp.Received(data) => {

      val packets = PacketsHelper.decode(data.toBitVector)

      packets.foreach(x => x match {
        case Left(p: Packet) => mqtt ! ReceivedPacket(p)
        case Right(p) => {
          sender ! Tcp.Close
        }
      })
    }

    case SendingPacket(p) => {

      val bits = PacketsHelper.encode(p)

      log.info("" + connection)
      connection ! bits.toTcpWrite
    }

    case Closing => {
      connection ! Tcp.Close
    }

    case f: Tcp.ConnectionClosed => {

      log.info("Connection closed " + f)
      context stop self
    }

    case x => {

      log.error("Unexpected " + x)
    }
  }
}

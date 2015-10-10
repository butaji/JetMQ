package net.jetmq.broker

import akka.actor.{ActorRef, Actor}
import akka.io.Tcp.{PeerClosed, Received}
import net.jetmq.Helpers._
import net.jetmq.packets._
import scodec.Codec


class RequestsHandler extends Actor {

  def connected(connection: ActorRef):Receive = {
    case Received(data) => {
      println("received data from " + connection + ": " + data.map("%02X" format _).mkString)

      val packet = Codec[Packet].decode(data.toArray.toBitVector).require.value

      println("received " + packet)

      packet match {
        case p: Connect => {
          println("Already connected")

          context stop self
        }
        case p: Disconnect => {
          println("Disconnect")

          context stop self
        }
        case p: Subscribe => {
          val back = Suback(Header(false, 0, false), p.message_identifier, p.topics.map(x => x._2))

          println("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case x => {
          println("Unexpected message " + x)

          context stop self
        }
      }
    }
    case x => {
      println("Unexpected message " + x)

      context become receive
    }
  }


  def receive = {

    case Received(data) => {

      println("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      val packet = Codec[Packet].decode(data.toArray.toBitVector).require.value

      println("received " + packet)

      packet match {
        case p: Connect => {
          val back = Connack(Header(false, 0, false), 0)

          println("sending back " + back)

          sender() ! Codec[Packet].encode(back).toTcpWrite

          context become connected(sender())
        }
        case x => {
          println("Unexpected message " + x)

          context stop self
        }
      }

    }

    case PeerClosed => {
      println("peer closed")

      context stop self
    }
  }

}

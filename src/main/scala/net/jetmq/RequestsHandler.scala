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

          p.topics.foreach(t =>
            MqttEventBusInstance.get.subscribe(self, t._1))

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case p: Publish => {

          if (p.header.qos == 1) {
            val back = Puback(Header(false, 0, false), p.message_identifier)

            println("sending back " + back)

            connection ! Codec[Packet].encode(back).toTcpWrite
          }

          if (p.header.qos == 2) {
            val back = Pubrec(Header(false, 0, false), p.message_identifier)

            println("sending back " + back)

            connection ! Codec[Packet].encode(back).toTcpWrite

          }

          MqttEventBusInstance.get.publish(MsgEnvelope(p.topic, PublishPayload(p)))
        }
        case p: Pubrel => {
          val back = Pubcomp(Header(false, 0, false), p.message_identifier)

          println("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case p : Unsubscribe => {
          val back = Unsuback(Header(false, 0, false), p.message_identifier)

          println("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case x => {
          println("Unexpected message " + x)

          context stop self
        }
      }
    }
    case PublishPayload(p) => {

      val back = Publish(p.header, p.topic, p.message_identifier, p.payload)

      println("sending back " + back)

      connection ! Codec[Packet].encode(back).toTcpWrite

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
          val result = if (p.clientId.length == 0 && p.connect_flags.clean_session == false)  2 else 0

          val back = Connack(Header(false, 0, false), result)

          println("sending back " + back)

          sender() ! Codec[Packet].encode(back).toTcpWrite

          if (result == 0) {
            context become connected(sender())
          }
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

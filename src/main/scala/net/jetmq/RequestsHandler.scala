package net.jetmq.broker

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import akka.io.Tcp.{PeerClosed, Received}
import net.jetmq.Helpers._
import net.jetmq.packets._
import scodec.Codec


class RequestsHandler(eventBus: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  def connected(connection: ActorRef):Receive = {
    case Received(data) => {
      log.info("received data from " + connection + ": " + data.map("%02X" format _).mkString)

      val packet = Codec[Packet].decode(data.toArray.toBitVector).require.value

      log.info("received " + packet)

      packet match {
        case p: Connect => {
          log.info("Already connected")

          context stop self
        }
        case p: Disconnect => {
          log.info("Disconnect")

          context stop self
        }
        case p: Subscribe => {
          p.topics.foreach(t =>
            eventBus ! BusSubscribe(t._1, self))

          val back = Suback(Header(false, 0, false), p.message_identifier, p.topics.map(x => x._2))

          log.info("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case p: Publish => {

          if (p.header.qos == 1) {
            val back = Puback(Header(false, 0, false), p.message_identifier)

            log.info("sending back " + back)

            connection ! Codec[Packet].encode(back).toTcpWrite
          }

          if (p.header.qos == 2) {
            val back = Pubrec(Header(false, 0, false), p.message_identifier)

            log.info("sending back " + back)

            connection ! Codec[Packet].encode(back).toTcpWrite

          }

          eventBus ! BusPublish(p.topic, p, p.header.retain)
        }
        case p: Pubrec => {
          val back = Pubrel(Header(false, 0, false), p.message_identifier)

          log.info("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case p: Pubrel => {
          val back = Pubcomp(Header(false, 0, false), p.message_identifier)

          log.info("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case p: Puback => {
          log.info("doing nothing for received " + p)
        }
        case p: Pubcomp => {
          log.info("doing nothing for received " + p)
        }
        case p : Unsubscribe => {
          p.topics.foreach(t =>
            eventBus ! BusUnsubscribe(t, self))

          val back = Unsuback(Header(false, 0, false), p.message_identifier)

          log.info("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
        case p: Pingreq => {
          val back = Pingresp(Header(false, 0, false))

          log.info("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }

        case x => {
          log.info("Unexpected message for connected " + x)

          context stop self
        }
      }
    }
    case x:PublishPayload => {

      x.payload match {
        case p: Publish => {
          val back = Publish(Header(p.header.dup, p.header.qos, x.auto), p.topic, p.message_identifier, p.payload)

          log.info("sending back " + back)

          connection ! Codec[Packet].encode(back).toTcpWrite
        }
      }
    }

    case x => {
      log.info("Unexpected row message for connected " + x)

      context become receive
    }
  }


  def receive = {

    case Received(data) => {

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      val packet = Codec[Packet].decode(data.toArray.toBitVector).require.value

      log.info("received " + packet)

      packet match {
        case p: Connect => {
          val result = if (p.clientId.length == 0 && p.connect_flags.clean_session == false)  2 else 0

          val back = Connack(Header(false, 0, false), result)

          log.info("sending back " + back)

          sender() ! Codec[Packet].encode(back).toTcpWrite

          if (result == 0) {
            context become connected(sender())
          }
        }
        case x => {
          log.info("Unexpected message for unconnected " + x)

          context stop self
        }
      }

    }

    case PeerClosed => {
      log.info("peer closed")

      context stop self
    }
  }

}

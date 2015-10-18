package net.jetmq

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import net.jetmq.broker.{BusPublish, BusSubscribe, BusUnsubscribe, PublishPayload}
import net.jetmq.packets._

class DeviceActor(event_bus: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  def receive = {

    case p: Connect => {

      val result = if (p.client_id.length == 0 && p.connect_flags.clean_session == false) 2 else 0

      val back = Connack(Header(false, 0, false), result)

      log.info("<- " + back)

      sender ! back

      if (result == 0) {
        context become connected(sender(), 1)
      }
    }

  }


  def connected(connection: ActorRef, message_id: Int): Receive = {
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
        event_bus ! BusSubscribe(t._1, self, t._2))

      val back = Suback(Header(false, 0, false), p.message_identifier, p.topics.map(x => x._2))

      log.info("<- " + back)

      connection ! back
    }
    case p: Publish => {

      if (p.header.qos == 1) {
        val back = Puback(Header(false, 0, false), p.message_identifier)

        log.info("<- " + back)

        connection ! back
      }

      if (p.header.qos == 2) {
        val back = Pubrec(Header(false, 0, false), p.message_identifier)

        log.info("<- " + back)

        connection ! back
      }

      event_bus ! BusPublish(p.topic, p, p.header.retain)
    }
    case p: Pubrec => {
      val back = Pubrel(Header(false, 1, false), p.message_identifier)

      log.info("<- " + back)

      connection ! back
    }
    case p: Pubrel => {
      val back = Pubcomp(Header(false, 0, false), p.message_identifier)

      log.info("<- " + back)

      connection ! back
    }
    case p: Puback => {
      log.info("doing nothing for received " + p)
    }
    case p: Pubcomp => {
      log.info("doing nothing for received " + p)
    }
    case p: Unsubscribe => {
      p.topics.foreach(t =>
        event_bus ! BusUnsubscribe(t, self))

      val back = Unsuback(Header(false, 0, false), p.message_identifier)

      log.info("<- " + back)

      connection ! back
    }
    case p: Pingreq => {
      val back = Pingresp(Header(false, 0, false))

      log.info("<- " + back)

      connection ! back
    }

    case x: PublishPayload => {

      x.payload match {
        case p: Publish => {
          val qos = p.header.qos min x.qos
          val back = Publish(Header(p.header.dup, qos, x.auto), p.topic, if (qos == 0) 0 else message_id, p.payload)

          log.info("<- " + back)

          connection ! back

          if (qos > 0) {
            context become connected(connection, message_id + 1)
          }
        }
      }
    }

    case x => {
      log.info("Unexpected message for connected " + x)

      context stop self
    }


  }

}

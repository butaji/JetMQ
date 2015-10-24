package net.jetmq

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import net.jetmq.broker.{PublishPayload, BusUnsubscribe, BusPublish, BusSubscribe}
import net.jetmq.packets._

class SessionActor(bus: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  context become receive(1)

  def receive = ???

  def receive(message_id: Int):Receive = {

    case EstablishConnection(p, persisted) => {
      val status = if (p.client_id.length == 0 && p.connect_flags.clean_session == false) 2 else 0
      val result = if (p.connect_flags.clean_session == false && persisted == true && status == 0) status + 256 else status

      sender ! Connack(Header(false, 0, false), result)

      if (status == 0) {
        context.parent ! DeviceConnection(p.client_id, self, sender)
        context become receive(message_id)
      }
    }
    case p: Disconnect => {
      log.info("Disconnect")

      context stop self
    }
    case p: Subscribe => {
      p.topics.foreach(t => bus ! BusSubscribe(t._1, self, t._2))

      sender ! Suback(Header(false, 0, false), p.message_identifier, p.topics.map(x => x._2))
    }
    case p: Publish => {

      if (p.header.qos == 1) {
        sender ! Puback(Header(false, 0, false), p.message_identifier)
      }

      if (p.header.qos == 2) {
        sender ! Pubrec(Header(false, 0, false), p.message_identifier)
      }

      bus ! BusPublish(p.topic, p, p.header.retain)
    }
    case p: Pubrec => {
      sender ! Pubrel(Header(false, 1, false), p.message_identifier)
    }
    case p: Pubrel => {
      sender ! Pubcomp(Header(false, 0, false), p.message_identifier)
    }
    case p: Puback => {
      log.info("doing nothing for received " + p)
    }
    case p: Pubcomp => {
      log.info("doing nothing for received " + p)
    }
    case p: Unsubscribe => {

      p.topics.foreach(t => bus ! BusUnsubscribe(t, self))

      sender ! Unsuback(Header(false, 0, false), p.message_identifier)
    }
    case p: Pingreq => {
      sender ! Pingresp(Header(false, 0, false))
    }

    case x: PublishPayload => {

      x.payload match {
        case p: Publish => {
          val qos = p.header.qos min x.qos

          val publish = Publish(Header(p.header.dup, qos, x.auto), p.topic, if (qos == 0) 0 else message_id, p.payload)
          context.parent ! PublishPayload(publish, x.auto, x.qos)

          if (qos > 0) {
            context become receive(message_id + 1)
          }
        }
      }
    }

    case x => {
      log.error("unexpected message for connected " + x)
    }
  }
}

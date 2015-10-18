package net.jetmq

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.io.Tcp.PeerClosed
import net.jetmq.broker.{BusPublish, BusSubscribe, BusUnsubscribe, PublishPayload}
import net.jetmq.packets._

class DeviceActor(event_bus: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  context become receive(1)

  def receive = ???

  def establishConnection(connection: ActorRef, p: Connect, message_id: Int) = {

    val result = if (p.client_id.length == 0 && p.connect_flags.clean_session == false) 2 else 0

    connection ! Connack(Header(false, 0, false), result)

    if (result == 0) {
      context become connected(connection, message_id)
    }
  }

  def receive(message_id: Int): Receive = {

    case EstablishConnection(connection, p) => {

      establishConnection(connection, p, message_id)
    }
    case x => {
      log.info("unexpected " + x)
    }
  }

  def connected(connection: ActorRef, message_id: Int): Receive = {
    case p: Connect => {
      log.info("Already connected")

      connection ! PeerClosed
    }
    case p: Disconnect => {
      log.info("Disconnect")

      context become receive(message_id)
    }
    case p: Subscribe => {
      p.topics.foreach(t =>
        event_bus ! BusSubscribe(t._1, self, t._2))

      connection ! Suback(Header(false, 0, false), p.message_identifier, p.topics.map(x => x._2))
    }
    case p: Publish => {

      if (p.header.qos == 1) {
        connection ! Puback(Header(false, 0, false), p.message_identifier)
      }

      if (p.header.qos == 2) {
        connection ! Pubrec(Header(false, 0, false), p.message_identifier)
      }

      event_bus ! BusPublish(p.topic, p, p.header.retain)
    }
    case p: Pubrec => {
      connection ! Pubrel(Header(false, 1, false), p.message_identifier)
    }
    case p: Pubrel => {
      connection ! Pubcomp(Header(false, 0, false), p.message_identifier)
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

      connection ! Unsuback(Header(false, 0, false), p.message_identifier)
    }
    case p: Pingreq => {
      connection ! Pingresp(Header(false, 0, false))
    }

    case x: PublishPayload => {

      x.payload match {
        case p: Publish => {
          val qos = p.header.qos min x.qos
          connection ! Publish(Header(p.header.dup, qos, x.auto), p.topic, if (qos == 0) 0 else message_id, p.payload)

          if (qos > 0) {
            context become connected(connection, message_id + 1)
          }
        }
      }
    }
    case EstablishConnection(c, p) => {

      if (c == connection) {
        connection ! PeerClosed
      } else {
        establishConnection(c, p, message_id)
      }
    }
    case x => {
      log.info("Unexpected message for connected " + x)
    }
  }
}

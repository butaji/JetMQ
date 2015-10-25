package net.jetmq

import akka.actor.{Stash, ActorRef, FSM}
import net.jetmq.broker._
import net.jetmq.packets._

case class ResetSession()

sealed trait SessionState

case object WaitingForNewSession extends SessionState

case object IdleSession extends SessionState

case object SessionConnected extends SessionState

sealed trait SessionBag { val message_id: Int }

case class WaitingBag(message_id: Int) extends SessionBag

case class SessionConnectedBag(connection: ActorRef, message_id: Int) extends SessionBag

case class ConnectionLost()
case class WrongState()

class SessionActor(bus: ActorRef) extends FSM[SessionState, SessionBag] with Stash {

  startWith(WaitingForNewSession, WaitingBag(1))

  when(WaitingForNewSession) {
    case Event(p: Connect, bag: WaitingBag) => {
      val result = if (p.client_id.length == 0 && p.connect_flags.clean_session == false) 2 else 0

      sender ! Connack(Header(false, 0, false), result)

      if (result == 0) {
        log.info("goto SessionConnected")
        goto(SessionConnected) using SessionConnectedBag(sender, bag.message_id)
      } else {

        stay
      }
    }

    case Event(p: Packet, _) => {
      sender ! WrongState()
      stay
    }
  }

  when(IdleSession) {

    case Event(p: Connect, bag: WaitingBag) => {
      val status = if (p.client_id.length == 0 && p.connect_flags.clean_session == false) 2 else 0
      val result = if (p.connect_flags.clean_session == false && status == 0) status + 256 else status

      sender ! Connack(Header(false, 0, false), result)

      if (status == 0) {
        unstashAll()
        log.info("goto SessionConnected")
        goto(SessionConnected) using SessionConnectedBag(sender, bag.message_id)
      } else {
        stay
      }
    }

    case Event(p: Packet, _) => {

      log.info("stashing " + p)

      stash()
      stay
    }
  }

  when(SessionConnected) {

    case Event(p: Subscribe, _) => {
      p.topics.foreach(t => bus ! BusSubscribe(t._1, self, t._2))

      sender ! Suback(Header(false, 0, false), p.message_identifier, p.topics.map(x => x._2))
      stay
    }

    case Event(p: Publish, _) => {

      if (p.header.qos == 1) {
        sender ! Puback(Header(false, 0, false), p.message_identifier)
      }

      if (p.header.qos == 2) {
        sender ! Pubrec(Header(false, 0, false), p.message_identifier)
      }

      bus ! BusPublish(p.topic, p, p.header.retain)
      stay
    }

    case Event(p: Pubrec, _) => {
      sender ! Pubrel(Header(false, 1, false), p.message_identifier)
      stay
    }

    case Event(p: Pubrel, _) => {
      sender ! Pubcomp(Header(false, 0, false), p.message_identifier)
      stay
    }

    case Event(p: Puback, _) => {
      log.info("doing nothing for received " + p)
      stay
    }

    case Event(p: Pubcomp, _) => {
      log.info("doing nothing for received " + p)
      stay
    }

    case Event(p: Unsubscribe, _) => {

      p.topics.foreach(t => bus ! BusUnsubscribe(t, self))

      sender ! Unsuback(Header(false, 0, false), p.message_identifier)
      stay
    }

    case Event(p: Pingreq, _) => {
      sender ! Pingresp(Header(false, 0, false))
      stay
    }

    case Event(x@PublishPayload(p: Publish, _, _), b: SessionConnectedBag) => {

      val qos = p.header.qos min x.qos

      val publish = Publish(Header(p.header.dup, qos, x.auto), p.topic, if (qos == 0) 0 else b.message_id, p.payload)
      b.connection ! publish

      if (qos > 0) {
        log.info("incrementing message id to " + (b.message_id + 1))
        log.info("goto SessionConnected")
        goto(SessionConnected) using (SessionConnectedBag(b.connection, b.message_id + 1))
      } else {
        stay
      }
    }
  }

  whenUnhandled {

    case Event(_:ConnectionLost, _) => {
      log.info("idle")

      log.info("goto IdleConnected")
      goto(IdleSession) using WaitingBag(1)
    }

    case Event(ResetSession | Disconnect, _) => {

      log.info("Resetting state")

      bus ! BusDeattach(self)

      log.info("goto WaitingForNewSession")
      goto(WaitingForNewSession) using (WaitingBag(1))
    }

    case Event(x, _) => {
      log.error("unexpected message " + x + " for " + stateName)

      log.info("goto WaitingForNewSession")
      goto(WaitingForNewSession) using (WaitingBag(1))
    }
  }
}

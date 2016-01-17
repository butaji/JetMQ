package net.jetmq.broker

import akka.actor.{ActorRef, FSM}

import scala.concurrent.duration._

case class ResetSession()

sealed trait SessionState

case object WaitingForNewSession extends SessionState

case object SessionConnected extends SessionState

sealed trait SessionBag {
  val message_id: Int
  val clean_session: Boolean
  val sending: List[Either[Publish, Pubrel]]
}

case class SessionWaitingBag(sending: List[Either[Publish, Pubrel]], stashed: List[Bus.PublishPayload], message_id: Int, clean_session: Boolean) extends SessionBag

case class SessionConnectedBag(connection: ActorRef, clean_session: Boolean, message_id: Int, last_packet: Long, sending: List[Either[Publish, Pubrel]], will: Option[Publish]) extends SessionBag

case object ConnectionLost

case object WrongState

case class CheckKeepAlive(period: FiniteDuration)
case object KeepAliveTimeout

case object TrySending

class SessionActor(bus_manager: ActorRef, bus_publisher: ActorRef) extends FSM[SessionState, SessionBag] {

  startWith(WaitingForNewSession, SessionWaitingBag(List(), List(), 1, clean_session = true))

  when(WaitingForNewSession) {
    case Event(c: Connect, bag: SessionWaitingBag) => {
      val status = if (c.client_id.length == 0 && c.connect_flags.clean_session == false) 2 else 0
      val result = if (c.connect_flags.clean_session == false && status == 0 && bag.clean_session == false) status + 256 else status

      sender ! Connack(Header(dup = false, 0, retain = false), result)

      if (status == 0) {

        if (c.connect_flags.clean_session == false) {
          log.info("current stashed is " + bag.stashed)
          bag.stashed.foreach(x => {
            log.info("publishing stashed " + x)
            self ! x
          })

          log.info("current sending is " + bag.sending)
          bag.sending.foreach {
            case Left(p) => {
              val pp = p.copy(header = p.header.copy(dup = true))
              log.info("publishing sending " + pp)
              sender ! pp
            }
            case Right(p) => {
              log.info("publishing sending " + p)
              sender ! p
            }
          }
        }

        if (c.connect_flags.keep_alive > 0) {
          val delay = c.connect_flags.keep_alive.seconds

          context.system.scheduler.scheduleOnce(delay, self, CheckKeepAlive(delay))(context.system.dispatcher)
        }

        val will = if (c.connect_flags.will_flag == true)
                    Some(
                      Publish(
                        Header(dup = false, c.connect_flags.will_qos, retain = c.connect_flags.will_retain), c.topic.get,0, c.message.get))
                    else None

        goto(SessionConnected) using SessionConnectedBag(sender, c.connect_flags.clean_session, bag.message_id,System.currentTimeMillis, List() ,will)
      } else {
        stay
      }
    }

    case Event(p: Packet, _) => {
      sender ! WrongState
      stay
    }

    case Event(p@Bus.PublishPayload(pp:Publish,_,_), b: SessionWaitingBag) => {


      if (pp.header.qos > 0) {

        val sp = p.copy(payload = pp.copy(header = pp.header.copy(dup = true)))
        log.info("stashing " + sp)

        goto(WaitingForNewSession) using b.copy(stashed = b.stashed :+ sp)
      } else {
        log.info("skipping stash " + p)

        stay
      }
    }
  }

  when(SessionConnected) {

    case Event(CheckKeepAlive(delay), b:SessionConnectedBag) => {

      log.info("Last package was " + new java.util.Date(b.last_packet))

      if ((System.currentTimeMillis() - delay.toMillis) < b.last_packet) {
        log.info("checking keepalive OK")

        context.system.scheduler.scheduleOnce(delay, self, CheckKeepAlive(delay))(context.system.dispatcher)

        stay
      } else {

        log.info("checking keepalive is NOT ok")

        b.connection ! KeepAliveTimeout

        self ! ConnectionLost

        stay
      }
    }

    case Event(p: Subscribe, b:SessionConnectedBag) => {
      p.topics.foreach(t => bus_manager ! Bus.Subscribe(t._1, self, t._2))

      sender ! Suback(Header(dup = false, 0, retain = false), p.message_identifier, p.topics.map(x => x._2))
      stay using b.copy(last_packet = System.currentTimeMillis())
    }

    case Event(p: Publish, b:SessionConnectedBag) => {

      if (p.header.qos == 1) {
        sender ! Puback(Header(dup = false, 0, retain = false), p.message_identifier)
      }

      if (p.header.qos == 2) {
        sender ! Pubrec(Header(dup = false, 0, retain = false), p.message_identifier)
      }

      if (p.payload.length == 0 && p.header.retain == false) {
        log.warning("got message with retain false and empty payload")
      }

      bus_publisher ! Bus.Publish(p.topic, p, p.header.retain, p.header.retain == true && p.payload.length == 0)

      stay using b.copy(last_packet = System.currentTimeMillis())
    }

    case Event(p: Pubrec, b:SessionConnectedBag) => {
      val pubrel = Pubrel(Header(dup = false, 1, retain = false), p.message_identifier)

      b.connection ! pubrel

      stay using b.copy(last_packet = System.currentTimeMillis(), sending = b.sending.filter(x => {
        x match {
          case Left(publish) if publish.message_identifier == p.message_identifier => true
          case _ => false
        }
      }) :+ Right(pubrel))
    }

    case Event(p: Pubrel, b:SessionConnectedBag) => {
      sender ! Pubcomp(Header(dup = false, 0, retain = false), p.message_identifier)

      stay using b.copy(last_packet = System.currentTimeMillis())
    }

    case Event(p: Puback, b:SessionConnectedBag) => {
      log.info("doing nothing for received " + p)

      stay using b.copy(last_packet = System.currentTimeMillis(), sending = b.sending.filter(x => {
        x match {
          case Left(publish) if publish.message_identifier == p.message_identifier => true
          case _ => false
        }
      }))
    }

    case Event(p: Pubcomp, b:SessionConnectedBag) => {
      log.info("doing nothing for received " + p)

      stay using b.copy(last_packet = System.currentTimeMillis(), sending = b.sending.filter(x => {
        x match {
          case Right(pubrel) if pubrel.message_identifier == p.message_identifier => true
          case _ => false
        }
      }))
    }

    case Event(p: Unsubscribe, b:SessionConnectedBag) => {

      p.topics.foreach(t => bus_manager ! Bus.Unsubscribe(t, self))

      sender ! Unsuback(Header(dup = false, 0, retain = false), p.message_identifier)

      stay using b.copy(last_packet = System.currentTimeMillis())
    }

    case Event(p: Pingreq, b:SessionConnectedBag) => {
      sender ! Pingresp(Header(dup = false, 0, retain = false))

      stay using b.copy(last_packet = System.currentTimeMillis())
    }

    case Event(x@Bus.PublishPayload(p: Publish, _, _), b: SessionConnectedBag) => {

      val qos = p.header.qos min x.qos

      val publish = Publish(Header(p.header.dup, qos, x.auto), p.topic, if (qos == 0) 0 else b.message_id, p.payload)

      b.connection ! publish

      if (qos > 0) {
        log.info("incrementing message id to " + (b.message_id + 1))
        stay using b.copy(message_id = b.message_id + 1, sending = b.sending :+ Left(publish))
      } else {
        stay
      }
    }

    case Event(ConnectionLost, b: SessionConnectedBag) => {
      log.info("idle")

      if (b.will.isDefined) {
        val p = b.will.get.copy(message_identifier = b.message_id)

        log.info("publishing will " + p)

        bus_publisher ! Bus.Publish(p.topic, p, p.header.retain, p.header.retain == true && p.payload.length == 0)
      }

      goto(WaitingForNewSession) using SessionWaitingBag(b.sending, List(), 1, b.clean_session)
    }
  }

  whenUnhandled {

    case Event(r@ResetSession(), b) => {

      log.info("Resetting state")

      if (b.clean_session == true)
        bus_manager ! Bus.Deattach(self)

      goto(WaitingForNewSession) using SessionWaitingBag(b.sending, List(), 1, b.clean_session)
    }


    case Event(d@Disconnect(_), b) => {

      log.info("Disonnecting state")

      if (b.clean_session == true) {
        bus_manager ! Bus.Deattach(self)
      }

      goto(WaitingForNewSession) using SessionWaitingBag(b.sending, List(), 1, b.clean_session)
    }


    case Event(x, b) => {
      log.error("unexpected message " + x + " for " + stateName)

      goto(WaitingForNewSession) using SessionWaitingBag(b.sending, List(), 1, b.clean_session)
    }
  }

  onTransition(handler _)

  def handler(from: SessionState, to: SessionState): Unit = {

    log.info("State changed from " + from + " to " + to)
  }

  initialize()
}

package net.jetmq.broker

import akka.actor.{ActorRef, FSM}
import akka.pattern.ask
import akka.util.Timeout
import net.jetmq.infra.PacketTrace

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

sealed trait ConnectionState

case object Active extends ConnectionState
case object Waiting extends ConnectionState

sealed trait ConnectionBag

case class EmptyConnectionBag() extends ConnectionBag

case class ConnectionSessionBag(session: ActorRef, connection: ActorRef) extends ConnectionBag

class MqttConnectionActor(sessions: ActorRef) extends FSM[ConnectionState, ConnectionBag] {

  val log_actor = context.system.actorSelection("akka://jetmq/system/*LogstashTcpUploader")

  startWith(Waiting, EmptyConnectionBag())

  when(Active) {
    case Event(p: Packet, bag: ConnectionSessionBag) => {

      log_actor ! PacketTrace(self.path.toString, false, p)

      bag.connection ! SendingPacket(p)
      stay
    }

    case Event(ReceivedPacket(c: Connect), bag: ConnectionSessionBag) => {
      log.info("Unexpected Connect. Closing peer")

      bag.session ! Disconnect(Header(dup = false, qos = 0, retain = false))

      bag.connection ! Closing
      stay
    }

    case Event(ReceivedPacket(c: Disconnect), bag: ConnectionSessionBag) => {
      log.info("Disconnect. Closing peer")

      bag.session ! Disconnect(Header(dup = false, qos = 0, retain = false))

      bag.connection ! Closing
      stay
    }

    case Event(ReceivedPacket(c: Packet), bag: ConnectionSessionBag) => {
      log_actor ! PacketTrace(self.path.toString, true, c)
      bag.session ! c
      stay
    }

    case Event(WrongState, b: ConnectionSessionBag) => {
      log.info("Session was in a wrong state")

      b.connection ! Closing
      stay
    }

    case Event(KeepAliveTimeout, b: ConnectionSessionBag) => {
      log.info("Keep alive timed out. Closing connection")

      b.connection ! Closing

      stay
    }
  }

  when(Waiting) {
    case Event(ReceivedPacket(c: Connect), _) => {
      implicit val timeout = Timeout(1 seconds)

      val sessionF: Future[ActorRef] = ask(sessions, c).mapTo[ActorRef]
      val session = Await.result(sessionF, timeout.duration)

      log_actor ! PacketTrace(self.path.toString, true, c)
      session ! c

      goto(Active) using ConnectionSessionBag(session, sender)
    }
    case x => {

      log.info("unexpected " + x + " for waiting. Closing peer")

      sender ! Closing

      stay
    }
  }

  whenUnhandled {

    case Event(x, _) => {
      log.error("unexpected " + x + " for " + stateName + ". Closing peer")

      sender ! Closing

      stay
    }
  }

  onTermination {
    case StopEvent(x, s, d) => {
      log.info("Terminated with " + x + " and " + s + " and " + d)
    }
  }

  onTransition(handler _)

  def handler(from: ConnectionState, to: ConnectionState): Unit = {
    log.info("State changed from " + from + " to " + to)
  }

  initialize()
}

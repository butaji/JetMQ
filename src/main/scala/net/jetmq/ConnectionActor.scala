package net.jetmq.broker

import akka.actor.{ActorRef, FSM}
import akka.io.Tcp.{Close, Closed, PeerClosed, Received}
import akka.pattern.ask
import akka.util.Timeout
import net.jetmq.Helpers._
import net.jetmq.packets.{Connect, Disconnect, Header, Packet}
import net.jetmq.{ConnectionLost, WrongState}
import scodec.Attempt.{Failure, Successful}
import scodec.DecodeResult

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

sealed trait ConnectionState

case object Waiting extends ConnectionState

case object Active extends ConnectionState

sealed trait ConnectionBag

case class EmptyConnectionBag() extends ConnectionBag

case class ConnectionSessionBag(session: ActorRef, connection: ActorRef) extends ConnectionBag

class ConnectionActor(sessions: ActorRef) extends FSM[ConnectionState, ConnectionBag] {

  startWith(Waiting, EmptyConnectionBag())

  when(Active) {
    case Event(p: Packet, bag: ConnectionSessionBag) => {
      log.info("<- " + p)
      val bits = PacketsHelper.encode(p)

      log.info("send data: " + bits.require.toByteArray.map("%02X" format _).mkString)

      bag.connection ! bits.toTcpWrite
      stay
    }

    case Event(Received(data), bag: ConnectionSessionBag) => {
      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      val p = PacketsHelper.decode(data.toBitVector)

      p match {
        case Successful(DecodeResult(x: Packet, _)) => log.info("-> " + x)
        case _ => {}
      }

      p match {
        case Successful(DecodeResult(c: Connect, _)) => {
          log.info("Unexpected Connect. Closing peer")

          bag.session ! Disconnect(Header(false, 0, false))

          bag.connection ! Close

          stay
        }
        case Successful(DecodeResult(c: Disconnect, _)) => {
          log.info("Disconnect. Closing peer")

          bag.session ! Disconnect(Header(false, 0, false))

          bag.connection ! Close

          stay
        }
        case Successful(DecodeResult(c: Packet, _)) => {
          bag.session ! c

          stay
        }
        case Failure(f) => {
          log.warning("Got failure " + f)

          bag.connection ! Close
          stay
        }
      }
    }

    case Event(PeerClosed, b: ConnectionSessionBag) => {
      log.info("peer closed")

      b.session ! ConnectionLost()

      stop
    }

    case Event(_: WrongState, b: ConnectionSessionBag) => {
      log.info("Session was in a wrong state")

      b.connection ! Close

      stay
    }
  }

  when(Waiting) {
    case Event(Received(data), _) => {

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      val p = PacketsHelper.decode(data.toBitVector)

      p match {
        case Successful(DecodeResult(x: Packet, _)) => log.info("-> " + x)
        case _ => {}
      }

      p match {
        case Successful(DecodeResult(c: Connect, _)) => {
          val sessionF: Future[ActorRef] = ask(sessions, c)(Timeout(1 second)).mapTo[ActorRef]
          val session = Await.result(sessionF, 1 second)

          session ! c

          goto(Active) using ConnectionSessionBag(session, sender)
        }
        case Failure(f) => {
          log.warning("Got failure " + f)

          sender ! Close
          stay
        }
        case x => {

          log.info("unexpected " + x + " for waiting. Closing peer")

          sender ! Close

          stay
        }
      }
    }
  }

  whenUnhandled {

    case Event(Closed, b) => {

      log.info("Closed for " + stateName + ". Stopping")

      stop
    }

    case Event(x, _) => {
      log.error("unexpected " + x + " for " + stateName + ". Closing peer")

      sender ! Close

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

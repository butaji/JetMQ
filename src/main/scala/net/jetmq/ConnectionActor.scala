package net.jetmq.broker

import akka.actor.{ActorRef, FSM}
import akka.io.Tcp.{Close, Closed, PeerClosed, Received}
import akka.pattern.ask
import akka.util.Timeout
import net.jetmq.Helpers._
import net.jetmq.packets.{Connect, Disconnect, Header, Packet}
import net.jetmq.{ConnectionLost, WrongState}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

sealed trait ConnectionState
case object Waiting extends ConnectionState
case object Active extends ConnectionState

sealed trait ConnectionBag
case class EmptyConnectionBag() extends ConnectionBag
case class ConnectionSessionBag(connection: ActorRef, session: ActorRef) extends ConnectionBag

class ConnectionActor(devices: ActorRef) extends FSM[ConnectionState, ConnectionBag] {

  startWith(Waiting, EmptyConnectionBag())

  when(Active) {
    case Event(p: Packet, bag:ConnectionSessionBag) => {
      log.info("<- " + p)
      bag.connection ! PacketsHelper.encode(p).toTcpWrite
      stay
    }

    case Event(Received(data), bag: ConnectionSessionBag) => {
      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      val bits = data.toArray.toBitVector
      val p = PacketsHelper.decode(bits)

      log.info("-> " + p)
      p match {
        case c:Connect => {
          log.info("Unexpected Connect. Closing peer")

          bag.session ! Disconnect(Header(false,0,false))

          bag.connection ! Close
          stay
        }
        case c:Disconnect => {
          log.info("Disconnect. Closing peer")

          bag.session ! Disconnect(Header(false,0,false))

          bag.connection ! Close

          context stop self

          stay
        }
        case _ => {
          bag.session ! p
          stay
        }
      }
    }

    case Event(PeerClosed | Closed, b: ConnectionSessionBag) => {
      log.info("peer closed")

      b.session ! ConnectionLost()

      stay
    }

    case Event(_:WrongState, b: ConnectionSessionBag) => {
      log.info("Session was in a wrong state")

      b.connection ! Close

      stay
    }

    case Event(x, b: ConnectionSessionBag) => {
      log.error("Unexpected message " + x + " for " + stateName)

      b.session ! Disconnect(Header(false, 0, false))

      b.connection ! Close

      stay
    }
  }

  when(Waiting) {
    case Event(Received(data), _) => {

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      val p = PacketsHelper.decode(data.toArray.toBitVector)

      log.info("-> " + p)
      p match {
        case c: Connect => {
          val sessionF: Future[ActorRef] = ask(devices, c)(Timeout(1 second)).mapTo[ActorRef]
          val session = Await.result(sessionF, 1 second)

          session ! c

          goto(Active) using ConnectionSessionBag(sender, session)
        }
        case x => {

          log.info("unexpected " + x + " for waiting. Closing peer")

          sender ! Close

          stay
        }
      }
    }

    case Event(PeerClosed | Closed, _) => {
      log.info("peer closed")

      //context stop self
      stay
    }
  }

  whenUnhandled {

    case Event(x, _) => {
      log.error("unexpected " + x + " for " + stateName + ". Closing peer")

      //context stop self ??
      stay
    }
  }

  onTermination {
    case StopEvent(x,s,d) => {
      log.info("Terminated with " + x + " and " + s + " and " + d)
    }
  }

  initialize()
}

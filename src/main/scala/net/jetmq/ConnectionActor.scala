package net.jetmq.broker

import akka.actor.{ActorRef, FSM}
import akka.io.Tcp.{PeerClosed, Received}
import akka.pattern.ask
import akka.util.Timeout
import net.jetmq.{WrongState, ConnectionLost}
import net.jetmq.Helpers._
import net.jetmq.packets.{Connect, Disconnect, Header, Packet}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

sealed trait ConnectionState
case object Waiting extends ConnectionState
case object Active extends ConnectionState

sealed trait ConnectionBag
case class EmptyConnectionBag() extends ConnectionBag
case class ConnectionConnectedBag(connection: ActorRef) extends ConnectionBag
case class ConnectionSessionBag(connection: ActorRef, session: ActorRef) extends ConnectionBag

class ConnectionActor(devices: ActorRef, coder: ActorRef) extends FSM[ConnectionState, ConnectionBag] {

  startWith(Waiting, EmptyConnectionBag())

  when(Active) {
    case Event(Decoded(p), bag: ConnectionSessionBag) => {
      log.info("-> " + p)
      bag.session ! p
      stay
    }
    case Event(Encoded(p), bag: ConnectionSessionBag) => {
      bag.connection ! p
      stay
    }
    case Event(p: Packet, _) => {
      log.info("<- " + p)
      coder ! p
      stay
    }

    case Event(Received(data), _) if (data.toArray.toBitVector == "e000".toBin.toBitVector) => {
      log.info("Disconnect. Closing peer")

      context stop self
      stay
    }

    case Event(Received(data), _) if (data.toArray.toBitVector.startsWith("10".toBin.toBitVector)) => {
      log.info("Unexpected Connect. Closing peer")

      context stop self
      stay
    }

    case Event(Received(data), _) => {
      val bits = data.toArray.toBitVector
      coder ! bits
      stay
    }

    case Event(PeerClosed, b: ConnectionSessionBag) => {
      log.info("peer closed")

      b.session ! ConnectionLost()

      context stop self
      stay
    }

    case Event(_:WrongState, _) => {
      log.info("Session was in a wrong state")

      context stop self
      stay
    }

    case Event(p: DecodingError, b: ConnectionSessionBag) => {
      log.error(p.exception, "closing connection")

      b.session ! Disconnect(Header(false, 0, false))
      context stop self
      stay
    }

    case Event(x, b: ConnectionSessionBag) => {
      log.error("Unexpected message " + x + " for " + stateName)

      b.session ! Disconnect(Header(false, 0, false))

      context stop self
      stay
    }
  }

  when(Waiting) {
    case Event(Received(data), _) => {

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      coder ! data.toArray.toBitVector

      goto(Waiting) using ConnectionConnectedBag(sender)
    }

    case Event(Decoded(c: Connect), b: ConnectionConnectedBag) => {

      val sessionF: Future[ActorRef] = ask(devices, c)(Timeout(1 second)).mapTo[ActorRef]
      val session = Await.result(sessionF, 1 second)

      self ! Decoded(c)

      goto(Active) using ConnectionSessionBag(b.connection, session)
    }

    case Event(x, _) => {
      log.info("unexpected " + x + " for waiting. Closing peer")

      context stop self
      stay
    }
  }


  initialize()
}

package net.jetmq.broker

import akka.actor.{Stash, ActorRef, FSM}
import akka.io.Tcp.{PeerClosed, Received}
import net.jetmq.{EstablishConnection, ConnectionLost}
import net.jetmq.Helpers._
import net.jetmq.packets.{Connect, Disconnect, Header, Packet}

sealed trait ConnectionState

case object Waiting extends ConnectionState
case object WaitingForSession extends ConnectionState
case object Active extends ConnectionState

case class ConnectionBag(val connection: Option[ActorRef], val session: Option[ActorRef] = None)

class ConnectionActor(devices: ActorRef, coder: ActorRef) extends FSM[ConnectionState, ConnectionBag] with Stash {

  startWith(Waiting, ConnectionBag(None, None))

  when(Active) {
    case Event( Decoded(p), bag) => {
      log.info("-> " + p)
      bag.session.get ! p
      stay
    }
    case Event( Encoded(p), bag) => {
      bag.connection.get ! p
      stay
    }
    case Event( p: Packet, _) => {
      log.info("<- " + p)
      coder ! p
      stay
    }

    case Event( Received(data), _) if (data.toArray.toBitVector == "e000".toBin.toBitVector) => {
      log.info("Disconnect. Closing peer")

      context stop self
      stay
    }

    case Event ( Received(data), _) if (data.toArray.toBitVector.startsWith("10".toBin.toBitVector)) => {
      log.info("Unexpected Connect. Closing peer")

      context stop self
      stay
    }

    case Event( Received(data), _) => {
      val bits = data.toArray.toBitVector
      coder ! bits
      stay
    }
  }

  when (WaitingForSession) {
    case Event( Decoded(c:Connect), _) => {
      devices ! c
      stay
    }

    case Event( e:EstablishConnection, bag) => {
      self ! Decoded(e.connect)
      unstashAll()
      goto(Active) using ConnectionBag(bag.connection, Some(e.session))
    }

    case _ => {
      stash()
      stay
    }
  }

  when (Waiting) {
    case Event( Received(data), _) => {

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      coder ! data.toArray.toBitVector

      goto(WaitingForSession) using ConnectionBag(Some(sender), None)
    }
  }

  whenUnhandled {

    case Event( PeerClosed, _) => {
      log.info("peer closed")

      devices ! ConnectionLost()
      context stop self
      stay
    }

    case Event( p:DecodingError, _) => {
      log.error(p.exception, "closing connection")

      devices ! Disconnect(Header(false, 0, false))
      context stop self
      stay
    }

    case Event(x, _) => {
      log.error("Unexpected message " + x + " for " + stateName)

      devices ! Disconnect(Header(false, 0, false))
      context stop self
      stay
    }
  }

  initialize()
}

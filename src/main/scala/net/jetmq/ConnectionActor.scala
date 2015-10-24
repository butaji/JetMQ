package net.jetmq.broker

import akka.actor.{ActorRef, FSM}
import akka.io.Tcp.{PeerClosed, Received}
import net.jetmq.ConnectionLost
import net.jetmq.Helpers._
import net.jetmq.packets.{Disconnect, Header, Packet}

sealed trait ConnectionState

case object Waiting extends ConnectionState
case object Active extends ConnectionState

case class ConnectionBag(val connection: ActorRef)

class ConnectionActor(devices: ActorRef, coder: ActorRef) extends FSM[ConnectionState, Option[ConnectionBag]] {

  startWith(Waiting, None)

  when(Active) {
    case Event( Decoded(p), _) => {
      log.info("-> " + p)
      devices ! p
      stay
    }
    case Event( Encoded(p), bag) => {
      bag.get.connection ! p
      stay
    }
    case Event( p: Packet, _) => {
      log.info("<- " + p)
      coder ! p
      stay
    }

    case Event( Received(data), _) if (data.toArray.toBitVector == "e000".toBin.toBitVector) => {
      log.info("Disconnect. Peer closed")

      devices ! Disconnect(Header(false, 0, false))
      context stop self
      stay
    }

    case Event( Received(data), _) => {
      val bits = data.toArray.toBitVector
      coder ! bits
      stay
    }
  }

  when (Waiting) {
    case Event( Received(data), _) => {

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      coder ! data.toArray.toBitVector

      goto(Active) using Some(ConnectionBag(sender))
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

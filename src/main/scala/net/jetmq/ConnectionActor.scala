package net.jetmq.broker

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.io.Tcp.{PeerClosed, Received}
import net.jetmq.ConnectionLost
import net.jetmq.Helpers._
import net.jetmq.packets.{Disconnect, Header, Packet}


class ConnectionActor(devices: ActorRef, coder: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  def connected(connection: ActorRef): Receive = {
    case Decoded(p) => {
      log.info("-> " + p)
      devices ! p
    }
    case Encoded(p) => {
      connection ! p
    }
    case p: Packet => {
      log.info("<- " + p)
      coder ! p
    }
    case Received(data) => {

      val bits = data.toArray.toBitVector
      if (bits == "e000".toBin.toBitVector) {
        log.info("Disconnect. Peer closed")

        devices ! Disconnect(Header(false, 0, false))
        context stop self
      }

      coder ! bits
    }
    case PeerClosed => {
      log.info("peer closed")

      devices ! ConnectionLost()
      context stop self
    }

    case p:DecodingError => {
      log.error(p.exception, "closing connection")

      devices ! Disconnect(Header(false, 0, false))
      context stop self
    }

    case x => {
      log.error("Unexpected message for connected " + x)

      devices ! Disconnect(Header(false, 0, false))
      context stop self
    }
  }

  def receive = {

    case Received(data) => {

      context become connected(sender)

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      coder ! data.toArray.toBitVector
    }

    case PeerClosed => {
      log.info("peer closed")

      devices ! ConnectionLost()
      context stop self
    }

    case x => {
      log.error("Unexpected message for unconnected " + x)

      devices ! Disconnect(Header(false, 0, false))
      context stop self
    }
  }
}

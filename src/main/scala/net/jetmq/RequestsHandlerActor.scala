package net.jetmq.broker

import akka.actor.Stash
import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.io.Tcp.{PeerClosed, Received}
import net.jetmq.DeviceConnected
import net.jetmq.Helpers._
import net.jetmq.packets.{Packet, Connect}


class RequestsHandlerActor(devices: ActorRef, coder: ActorRef) extends Actor with Stash {

  val log = Logging.getLogger(context.system, this)

  def connected(device: ActorRef, connection: ActorRef): Receive = {
    case Decoded(p) => {

      log.info("-> " + p)
      device ! p
    }
    case Encoded(p) => {
      connection ! p
    }
    case p: Packet => {
      log.info("<- " + p)
      coder ! p
    }
    case Received(data) => {
      coder ! data.toArray.toBitVector
    }
    case PeerClosed => {
      log.info("peer closed")

      context stop self
    }

    case x => {
      log.info("Unexpected message for connected " + x)

      context stop self
    }
  }


  def online(connection: ActorRef): Receive = {
    case DeviceConnected(d) => {

      unstashAll()
      context become connected(d, connection)
    }

    case Decoded(p: Connect) => {
      devices ! p
    }

    case x => {
      log.info("stashing " + x)
      stash()
    }
  }

  def receive = {

    case Received(data) => {

      context become online(sender)

      log.info("received data from" + sender() + ": " + data.map("%02X" format _).mkString)

      coder ! data.toArray.toBitVector
    }

    case PeerClosed => {
      log.info("peer closed")

      context stop self
    }

    case x => {
      log.info("Unexpected message for unconnected " + x)

      context stop self
    }
  }
}

package net.jetmq.broker

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.io.Tcp.{PeerClosed, Received}
import net.jetmq.DeviceConnected
import net.jetmq.Helpers._
import net.jetmq.packets._


class RequestsHandlerActor(devices: ActorRef, coder: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  def connected(device: ActorRef): Receive = {
    case p: Packet => {
      device ! p
    }
  }

  def receive = {

    case DeviceConnected(d) => {

      context become connected(d)
    }

    case Received(data) => {

      context become connected(sender)

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

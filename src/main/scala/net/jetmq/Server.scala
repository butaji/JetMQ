package net.jetmq.broker

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.io.{IO, Tcp}

class Server extends Actor {

  import Tcp._
  import context.system

  val log = Logging.getLogger(context.system, this)

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 1883))

  def receive = {

    case b@Bound(localAddress) => {
      log.info("bound to " + localAddress)
    }

    case CommandFailed(_: Bind) => {
      log.info("command failed")
      context stop self
    }

    case c@Connected(remote, local) => {

      log.info("client connected " + remote)

      val bus = system.actorOf(Props[EventBusActor])
      val handler = system.actorOf(Props(new RequestsHandler(bus)))
      val connection = sender()
      connection ! Register(handler)
    }
  }

}

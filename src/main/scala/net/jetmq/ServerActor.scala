package net.jetmq.broker

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import net.jetmq.SessionsManagerActor

class ServerActor extends Actor {

  import context.system

  val bus = system.actorOf(Props[EventBusActor], name = "event-bus")
  val sessions = system.actorOf(Props(new SessionsManagerActor(bus)), name = "sessions")

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

      val coder = system.actorOf(Props[PacketsActor])
      val handler = system.actorOf(Props(new ConnectionActor(sessions, coder)))
      val connection = sender()
      connection ! Register(handler)
    }
  }
}

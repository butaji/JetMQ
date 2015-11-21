package net.jetmq.broker

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}

class ServerActor extends Actor with ActorLogging {

  import context.system

  val bus = system.actorOf(Props[EventBusActor], name = "event-bus")
  val sessions = system.actorOf(Props(new SessionsManagerActor(bus)), name = "sessions")

  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0", 1883))

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

      val handler = system.actorOf(
        Props(new TcpConnectionActor(sessions)).withMailbox("priority-dispatcher"),
        remote.getHostString + ":" + remote.getPort)
      sender ! Register(handler)
    }
  }
}

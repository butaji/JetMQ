package net.jetmq.broker

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 1883))

  def receive = {

    case b@Bound(localAddress) => {
      println("bound to " + localAddress)

    }

    case CommandFailed(_: Bind) => {
      println("command failed")
      context stop self
    }

    case c@Connected(remote, local) => {

      println("client connected " + remote)

      val handler = context.actorOf(Props[RequestsHandler])
      val connection = sender()
      connection ! Register(handler)
    }
  }

}

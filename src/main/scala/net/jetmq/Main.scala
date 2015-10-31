package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.event.Logging

object Main extends App {

  val system = ActorSystem("jetmq")

  val server = system.actorOf(Props[ServerActor], "server")

  val log = Logging.getLogger(system, this)
  log.info("started...")
}

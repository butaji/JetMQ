package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

object Main extends App {

  val config = ConfigFactory.load()
  val system = ActorSystem("jetmq", config)
  val log = Logging.getLogger(system, this)

  val server = system.actorOf(Props[ServerActor], "server")

  log.info("started...") 
}

package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

object Main extends App {

  val config = ConfigFactory.load
  implicit val system = ActorSystem("jetmq", config.getConfig("akka"))

  val server = system.actorOf(Props[ServerActor], "server")

  val log = Logging.getLogger(system, this)
  log.info("started...")
}

package net.jetmq.broker

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.config.ConfigFactory

object Main extends App {

  val config = ConfigFactory.load()
  val system = ActorSystem("jetmq", config)
  val log = Logging.getLogger(system, this)

  BrokerServer.run(system)

  log.info("started...")
}

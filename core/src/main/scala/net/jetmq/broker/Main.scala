package net.jetmq.broker

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import kamon.Kamon

object Main extends App {

  val config = ConfigFactory.load()

  Kamon.start(config)

  val system = ActorSystem("jetmq", config)
  val log = Logging.getLogger(system, this)

  BrokerServer.run(system)

  log.info("started...")
}

package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.event.Logging

/**
 * Created by vitalybaum on 04/10/15.
 */
object Main extends App {

  implicit val system = ActorSystem("first-run")

  val server = system.actorOf(Props[Server], "server")

  val log = Logging.getLogger(system, this)
  log.info("started...")
}

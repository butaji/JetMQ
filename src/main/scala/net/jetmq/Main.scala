package net.jetmq.broker

import akka.actor.{ActorSystem, Props}

/**
 * Created by vitalybaum on 04/10/15.
 */
object Main extends App {

  implicit val system = ActorSystem("first-run")

  val server = system.actorOf(Props[Server], "server")

  println("started...")
}

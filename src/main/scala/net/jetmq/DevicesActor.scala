package net.jetmq

import java.net.URLEncoder
import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.jetmq.packets.Connect

case class DeviceConnected(device: ActorRef)
case class EstablishConnection(connection: ActorRef, connect: Connect)

class DevicesActor(bus: ActorRef) extends Actor{

  val log = Logging.getLogger(context.system, this)

  def getActorName(client_id: String): String = {

    val cid = if (client_id.isEmpty) UUID.randomUUID().toString else client_id
    URLEncoder.encode(cid, "utf-8")
  }

  def getOrCreate(actor_name: String): ActorRef = {

    val c = context.child(actor_name)
    if (c.isDefined) c.get else context.actorOf(Props(new DeviceActor(bus)), name = actor_name)
  }

  def receive = {

    case p: Connect => {

      val name = getActorName(p.client_id)
      val device = getOrCreate(name)

      sender() ! DeviceConnected(device)
      device ! EstablishConnection(sender(), p)
    }

    case x => {
      log.info("Unexpected " + x)

    }
  }
}

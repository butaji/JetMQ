package net.jetmq

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.jetmq.packets.Connect

case class DeviceConnected(device: ActorRef)
case class EstablishConnection(connection: ActorRef)

class DevicesActor(bus: ActorRef) extends Actor{

  val log = Logging.getLogger(context.system, this)

  def getOrCreate(client_id: String): ActorRef = {
      val c = context.child(client_id)

    if (c.isDefined) c.get else context.actorOf(Props(new DeviceActor(bus)), name = client_id)
  }

  def receive = {

    case p: Connect => {

      val device = getOrCreate(p.client_id)

      sender() ! DeviceConnected(device)
      device ! EstablishConnection(sender())
      device ! p
    }
  }
}

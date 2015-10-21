package net.jetmq

import java.net.URLEncoder
import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.PeerClosed
import net.jetmq.broker.{BusDeattach, PublishPayload}
import net.jetmq.packets._

private case class DeviceConnection(name: String, device: ActorRef, connection: ActorRef)
case class EstablishConnection(connect: Connect, persisted: Boolean)
case class ConnectionLost()

class DevicesActor(bus: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  def getActorName(client_id: String): String = {

    val cid = if (client_id.isEmpty) UUID.randomUUID().toString else client_id
    URLEncoder.encode(cid, "utf-8")
  }

  def getOrCreate(actor_name: String): ActorRef = {

    val c = context.child(actor_name)
    if (c.isDefined) c.get else context.actorOf(Props(new DeviceActor(bus)), name = actor_name)
  }

  context become receive(List())

  def receive = ???

  def receive(connections: List[DeviceConnection]): Receive = {

    case p: Connect => {

      val name = getActorName(p.client_id)

      val with_same_name_and_connection = connections.filter(t => t.connection == sender).toArray

      if (with_same_name_and_connection.length > 0) {
        sender ! PeerClosed
      } else {
        val device = getOrCreate(name)

        log.info("connections are " + connections)
        device forward EstablishConnection(p, connections.count(t => t.name == name) > 0)
      }
    }

    case p: DeviceConnection => {
      context become receive(p :: connections.filter(t => t.name != p.name))
    }

    case p: ConnectionLost => {
      context become receive(connections.filter(t => t.connection != sender))
    }

    case p: Disconnect => {

      context become receive(connections.filter(t => t.connection != sender))

      connections.filter(t => t.connection == sender)
        .map(t => t.device)
        .foreach(t => {
          t forward p
          bus ! BusDeattach(t)
        })
    }

    case p: Packet => {

      val connected = connections.filter(t => t.connection == sender).map(t => t.device).toArray

      if (connected.length == 1) {
        connected.foreach(t => t forward p)
      } else {
        sender ! PeerClosed
      }
    }

    case x: PublishPayload => {
      connections.filter(t => t.device == sender).foreach(t => t.connection ! x.payload)
    }

    case x => {
      log.error("Unexpected " + x)
    }
  }
}

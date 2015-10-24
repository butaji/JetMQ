package net.jetmq

import java.net.URLEncoder
import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import net.jetmq.packets._

case class EstablishConnection(connect: Connect, session: ActorRef)
case class ConnectionLost()

class SessionsManagerActor(bus: ActorRef) extends Actor {

  val log = Logging.getLogger(context.system, this)

  def getActorName(client_id: String): String = {

    val cid = if (client_id.isEmpty) UUID.randomUUID().toString else client_id
    URLEncoder.encode(cid, "utf-8")
  }

  def getOrCreate(actor_name: String, clean_session: Boolean): ActorRef = {

    val c = context.child(actor_name)
    if (c.isDefined && clean_session == false) {
      return c.get
    }

    if (c.isDefined && clean_session == true) {
      c.get ! ResetSession
      return c.get
    }

    context.actorOf(Props(new SessionActor(bus)), name = actor_name)
  }

  def receive = {

    case p: Connect => {

      val name = getActorName(p.client_id)

      val device = getOrCreate(name, p.connect_flags.clean_session)

      sender ! EstablishConnection(p, device)
    }

    case x => {
      log.error("Unexpected " + x)
    }
  }
}

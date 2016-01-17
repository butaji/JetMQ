package net.jetmq.broker

import java.net.URLEncoder
import java.util.UUID
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class SessionsManagerActor(bus_manager: ActorRef, bus_publisher: ActorRef) extends Actor with ActorLogging {

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

    val a = context.actorOf(Props(new SessionActor(bus_manager, bus_publisher)), name = actor_name)

    context.watch(a)

    return a
  }

  def receive = {

    case p: Connect => {

      val name = getActorName(p.client_id)

      val device = getOrCreate(name, p.connect_flags.clean_session)

      sender ! device
    }

    case x => {
      log.error("Unexpected " + x)
    }
  }
}

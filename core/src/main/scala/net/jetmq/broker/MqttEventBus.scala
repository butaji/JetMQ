package net.jetmq.broker

import akka.actor.{Actor, ActorLogging, ActorRef}

object Bus {

  case class Subscribe(topic: String, actor: ActorRef, qos: Int = 0)

  case class Unsubscribe(topic: String, actor: ActorRef)

  case class Publish(topic: String, payload: Any, retain: Boolean = false, clean_retain: Boolean = false)

  case class Deattach(actor: ActorRef)

  case class PublishPayload(payload: Any, auto: Boolean, qos: Int = 0)

  case class BadSubscriptionException(msg: String) extends Throwable
}

class EventBusActor extends Actor with ActorLogging {

  context become working(List(), List())

  def working(subscriptions: List[(String, ActorRef, Int)], retains: List[(String, Any)]): Receive = {

    case p: Bus.Subscribe => {
      log.info("subscribe " + p)
      MqttTopicClassificator.checkTopicName(p.topic)

      val similar = subscriptions.filter(t => (t._1 == p.topic) && (t._2 == p.actor)).toArray
      if (similar.length == 0) {
        context become working((p.topic, p.actor, p.qos) :: subscriptions, retains)

        log.info("subscribed to " + p.topic)
      } else {
        log.info("subscription already exists " + p)

        log.info("changing existing subscription qos to " + p.qos)

        context become working((p.topic, p.actor, p.qos) :: subscriptions.filter(t => !(t._1 == p.topic && t._2 == p.actor)), retains)
      }

      retains
        .filter(t => MqttTopicClassificator.isSubclass(t._1, p.topic))
        .sortBy(t => t._1)
        .foreach(t => {

          p.actor ! Bus.PublishPayload(t._2, auto = true, qos = p.qos)
        })
    }
    case p: Bus.Unsubscribe => {
      log.info("unsubscribe " + p)
      context become working(subscriptions.filter(t => !(t._1 == p.topic && t._2 == p.actor)), retains)
    }

    case p: Bus.Deattach => {
      log.info("clearing bus for " + p)
      context become working(subscriptions.filter(t => t._2 != p.actor), retains)
    }
    case p: Bus.Publish => {

      log.info("got " + p)
      log.info("current subscriptions " + subscriptions.map(t => t._1 + "@" + t._3).mkString(", "))

      subscriptions
        .filter(t => MqttTopicClassificator.isSubclass(p.topic, t._1))
        .groupBy(t => t._2)
        .map(t => (t._1, t._2.toArray))
        .foreach(t => {
          log.info("publish " + p + " by subscriptions: " + t._2.map(x => x._1 + "@" + x._3).mkString(", "))
          val max_qos = t._2.map(x => x._3).max
          t._1 ! Bus.PublishPayload(p.payload, auto = false, qos = max_qos)
        })

      if (p.retain == true) {
        if (p.clean_retain == true) {

          log.info("cleaning retain for " + p.topic)
          context become working(subscriptions, retains.filter(x => x._1 != p.topic))
        } else {
          context become working(subscriptions, (p.topic, p.payload) :: retains.filter(x => x._1 != p.topic))
        }
      }
    }

  }

  def receive = {

    case x => {
      log.error("It was unexcepted " + x)
    }
  }
}



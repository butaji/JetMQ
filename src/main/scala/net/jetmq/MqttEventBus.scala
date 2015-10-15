package net.jetmq.broker

import akka.actor.{Actor, ActorRef}
import akka.event.Logging

case class BusSubscribe(topic: String, actor: ActorRef)
case class BusUnsubscribe(topic: String, actor: ActorRef)

case class BusPublish(topic: String, payload: Any, retain: Boolean = false)

case class PublishPayload(payload: Any, auto: Boolean)

class EventBusActor extends Actor {

  val log = Logging.getLogger(context.system, this)

  context become working(List(), List())

  def working(subscriptions: List[(String, ActorRef)], retains: List[(String, Any)]): Receive = {
    case p: BusSubscribe => {
      log.info("subscribe " + p)
      MqttTopicClassificator.checkTopicName(p.topic)

      if (!subscriptions.exists(t => (t._1 == p.topic) && (t._2 == p.actor))) {
        context become working((p.topic, p.actor) :: subscriptions, retains)
      } else {
        log.info("subscription already exists " + p)
      }

      retains
        .filter(t => MqttTopicClassificator.isSubclass(t._1, p.topic))
        .foreach(t => {

          p.actor ! PublishPayload(t._2, true)
        })
    }
    case p: BusUnsubscribe => {
      log.info("unsubscribe " + p)
      context become working(subscriptions.filter(t => !(t._1 == p.topic && t._2 == p.actor)), retains)
    }
    case p: BusPublish => {

      subscriptions
        .filter(t => MqttTopicClassificator.isSubclass(p.topic, t._1))
        .groupBy(t => t._2)
        .map(t => (t._1, t._2.toArray ))
        .foreach(t => {
          log.info("publish " + p + " by subscriptions " + t._2)
          t._1 ! PublishPayload(p.payload, false)
        })

      if (p.retain == true) {
        context become working(subscriptions, (p.topic, p.payload) :: retains.filter(x => x._1 != p.topic))
      }
    }

  }

  def receive = {

    case x => {
      log.info("It was unexcepted " + x)
    }
  }
}

object MqttTopicClassificator {

  def checkTopicName(to: String):Boolean = {
    if (to != "#" && to.contains("#") &&
      (to.replace("#", "/#") != to.replace("/#", "//#") || to.last != '#')) {
      throw new BadSubscriptionException(to)
    }

    if (to != "+" && to.contains("+") && (to.charAt(0) != '+') && to.replace("+", "/+") != to.replace("/+", "//+")) {
      throw new BadSubscriptionException(to)
    }

    if (to.length > 0 && to.charAt(0) == '+')
      checkTopicName(to.substring(1))

    return true
  }

  def isSubclass(actual: String, subscribing: String): Boolean = {

    if (!subscribing.contains('#') && !subscribing.contains('+'))
      return isPlainSubclass(actual, subscribing)

    if (subscribing == "#")
      return true;

    val square_index = subscribing.indexOf('#')

    if (square_index > 0) {
      val sub = if (square_index > 1) subscribing.substring(0, square_index - 1) + ".*" else "/.*"

      return isRegexSubclass(actual, sub)
    }

    return isRegexSubclass(actual, subscribing)
  }

  private def isPlainSubclass(actual: String, subscribing: String): Boolean =
    subscribing == actual

  private def isRegexSubclass(actual: String, subscribing: String): Boolean = {

    val reg = subscribing.zipWithIndex.map {
      case (c,i) => {
        if (c == '+')
          if (i == 0 || i == (subscribing.length-1)) "[^/]*" else "[^/]+"
        else
          c.toString
      }
    }.mkString

    val res = actual.matches(reg)

    res
  }

  private def isSquaredSubclass(actual: String, sub: String) = {
    actual.startsWith(sub)
  }
}

class BadSubscriptionException(msg: String) extends Throwable

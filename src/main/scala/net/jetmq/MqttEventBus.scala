package net.jetmq.broker

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import net.jetmq.packets.Publish

case class BusSubscribe(topic: String, actor: ActorRef)
case class BusUnsubscribe(topic: String, actor: ActorRef)

case class BusPublish(topic: String, payload: Any)

case class PublishPayload(payload: Publish)

class EventBusActor extends Actor {

  val log = Logging.getLogger(context.system, this)

  def working(subscriptions: List[(String, ActorRef)]): Receive = {
    case p: BusSubscribe => {
      log.info("subscribe " + p)
      MqttTopicClassificator.checkTopicName(p.topic)

      if (subscriptions.filter(t => (t._1 == p.topic) && (t._2 == p.actor)).length == 0)
        context become working((p.topic, p.actor) :: subscriptions)
      else
        log.info("subscription is already exist" + p)
    }
    case p: BusUnsubscribe => {
      log.info("unsubscribe " + p)
      context become working(subscriptions.filter(t => t._1 == p.topic && t._2 == p.actor))
    }
    case p: BusPublish => {

      subscriptions
        .filter(t => MqttTopicClassificator.isSubclass(p.topic, t._1))
        .groupBy(t => t._2)
        .map(t => (t._1, t._2.toArray ))
        .foreach(t => {
          log.info("publish " + p + " by subscriptions " + t._2)
          t._1 ! p.payload
        })
    }

  }

  def receive = {
    case p: BusSubscribe => {

      log.info("subscribe " + p)

      MqttTopicClassificator.checkTopicName(p.topic)

      context become working(List((p.topic, p.actor)))
    }
    case p: BusUnsubscribe => {
      log.info("got " + p + " but there are no subscriptions")
    }
    case p: BusPublish => {
      log.info("got " + p + " but there are no subscriptions")
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
      val sub = subscribing.substring(0, square_index - 1) + ".*"

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

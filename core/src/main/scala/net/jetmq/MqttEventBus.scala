package net.jetmq.broker

import akka.actor.{Actor, ActorLogging, ActorRef}

case class BusSubscribe(topic: String, actor: ActorRef, qos: Int = 0)

case class BusUnsubscribe(topic: String, actor: ActorRef)

case class BusPublish(topic: String, payload: Any, retain: Boolean = false, clean_retain: Boolean = false)

case class BusDeattach(actor: ActorRef)

case class PublishPayload(payload: Any, auto: Boolean, qos: Int = 0)

class EventBusActor extends Actor with ActorLogging {

  context become working(List(), List())

  def working(subscriptions: List[(String, ActorRef, Int)], retains: List[(String, Any)]): Receive = {
    case p: BusSubscribe => {
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

          p.actor ! PublishPayload(t._2, true, p.qos)
        })
    }
    case p: BusUnsubscribe => {
      log.info("unsubscribe " + p)
      context become working(subscriptions.filter(t => !(t._1 == p.topic && t._2 == p.actor)), retains)
    }

    case p: BusDeattach => {
      log.info("clearing bus for " + p)
      context become working(subscriptions.filter(t => t._2 != p.actor), retains)
    }
    case p: BusPublish => {

      log.info("got " + p)
      log.info("current subscriptions " + subscriptions.map(t => t._1 + "@" + t._3).mkString(", "))

      subscriptions
        .filter(t => MqttTopicClassificator.isSubclass(p.topic, t._1))
        .groupBy(t => t._2)
        .map(t => (t._1, t._2.toArray))
        .foreach(t => {
          log.info("publish " + p + " by subscriptions: " + t._2.map(x => x._1 + "@" + x._3).mkString(", "))
          val max_qos = t._2.map(x => x._3).reduceLeft(_ max _)
          t._1 ! PublishPayload(p.payload, false, max_qos)
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

object MqttTopicClassificator {

  def checkTopicName(to: String): Boolean = {
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
      case (c, i) => {
        if (c == '+')
          if (i == 0 || i == (subscribing.length - 1)) "[^/]*" else "[^/]+"
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

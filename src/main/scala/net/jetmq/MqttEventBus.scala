package net.jetmq.broker

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification
import net.jetmq.packets.Publish

final case class MsgEnvelope(topic: String, payload: Any)

case class PublishPayload(message: Publish)

object MqttEventBusInstance {
  val get = new MqttEventBus
}

class MqttEventBus extends EventBus with SubchannelClassification {
  type Event = MsgEnvelope
  type Classifier = String
  type Subscriber = ActorRef

  override protected val subclassification: Subclassification[Classifier] =
    new MqttTopicClassification

  // is used for extracting the classifier from the incoming events
  override protected def classify(event: Event): Classifier = event.topic

  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event.payload
  }

  override def subscribe(subscriber: ActorRef, to: String): Boolean = {

    if (to != "#" && to.contains("#") &&
          (to.replace("#", "/#") != to.replace("/#", "//#") || to.last != '#')) {
       throw new BadSubscriptionException(to)
    }

    super.subscribe(subscriber, to)
  }
}

class MqttTopicClassification extends Subclassification[String] {
  override def isEqual(x: String, y: String): Boolean =
    x == y

  override def isSubclass(actual: String, subscribing: String): Boolean = {

    if (!subscribing.contains('#') && !subscribing.contains('+'))
      return isPlainSubclass(actual, subscribing)

    if (subscribing == "#")
      return true;

    val square_index = subscribing.indexOf('#')

    if (square_index > 0) {
      val sub = subscribing.substring(0, square_index - 1)

      return isSquaredSubclass(actual, sub) || isRegexSubclass(actual, sub)
    }

    return isRegexSubclass(actual, subscribing)
  }

  def isPlainSubclass(actual: String, subscribing: String): Boolean =
    subscribing == actual

  def isRegexSubclass(actual: String, subscribing: String): Boolean = {
    val reg = subscribing.replaceAll("\\+", "[^/]+")
    val res = actual.matches(reg)

    res
  }

  def isSquaredSubclass(actual: String, sub: String) = {
    actual.startsWith(sub)
  }

}

class BadSubscriptionException(msg: String) extends Throwable

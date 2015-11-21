package net.jetmq.tests

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.broker._
import org.specs2.mutable._
import org.specs2.specification.Scope

class BusSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  isolated

  "MqttEventBus handler actor" should {

    "simple pubsub" in {

      val bus = system.actorOf(Props[EventBusActor])

      bus ! BusSubscribe("greetings", self)
      bus ! BusPublish("time", "123")
      bus ! BusPublish("greetings", "hello")

      expectMsg(PublishPayload("hello", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "4.7.1.2 Multi-level wildcard" in {

      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/tennis/player1/#", self)
      bus ! BusPublish("sport/tennis/player1", "1")
      expectMsg(PublishPayload("1", false))

      bus ! BusPublish("sport/tennis/player1/ranking", "2")
      expectMsg(PublishPayload("2", false))

      bus ! BusPublish("sport/tennis/player1/score/wimbledon", "3")
      expectMsg(PublishPayload("3", false))

      expectNoMsg(Bag.wait_time)
      success
    }


    "support multilevel only with a wildcard" in {

      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/tennis/player2", self)
      bus ! BusPublish("sport/tennis/player2", "1")
      expectMsg(PublishPayload("1", false))

      bus ! BusPublish("sport/tennis/player2/ranking", "2")

      expectNoMsg(Bag.wait_time)
      success
    }

    "square for parent level" in {
      //“sport/#” also matches the singular “sport”, since # includes the parent level
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/#", self)
      bus ! BusPublish("sport", "1")
      expectMsg(PublishPayload("1", false))

      expectNoMsg(Bag.wait_time)

      bus ! BusSubscribe("#", self)
      bus ! BusPublish("sport", "2")
      expectMsg(PublishPayload("2", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "square is root" in {
      //“#” is valid and will receive every Application Message
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("#", self)
      bus ! BusPublish("sport", "1")
      expectMsg(PublishPayload("1", false))

      bus ! BusPublish("dw", "2")
      expectMsg(PublishPayload("2", false))

      bus ! BusPublish("dw/1/2/3/values", "3")
      expectMsg(PublishPayload("3", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "work for valid input" in {
      //“sport/tennis/#” is valid

      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/tennis/#", self)
      bus ! BusPublish("sport/tennis/123", "1")
      expectMsg(PublishPayload("1", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "“sport/tennis#” is not valid" in {
      MqttTopicClassificator.checkTopicName("sport/tennis#") must throwA[BadSubscriptionException]
    }

    "“sport/tennis/#/ranking”" in {

      MqttTopicClassificator.checkTopicName("sport/tennis/#/ranking") must throwA[BadSubscriptionException]
    }

    "check only one level for plus" in {
      //“sport/tennis/+” matches “sport/tennis/player1” and “sport/tennis/player2”, but not “sport/tennis/player1/ranking”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/tennis/+", self)
      bus ! BusPublish("sport/tennis/player1", "1")
      expectMsg(PublishPayload("1", false))

      bus ! BusPublish("sport/tennis/player2", "2")
      expectMsg(PublishPayload("2", false))

      bus ! BusPublish("sport/tennis/player1/ranking", "3")

      expectNoMsg(Bag.wait_time)
      success
    }

    "check no parent level for plus" in {
      //“sport/+” does not match “sport” but it does match “sport/”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/+", self)
      bus ! BusPublish("sport", "1")
      expectNoMsg(Bag.wait_time)

      bus ! BusPublish("sport/", "2")
      expectMsg(PublishPayload("2", false))

      success
    }

    "check level for double plus" in {
      //“/finance” matches “+/+”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("+/+", self)
      bus ! BusPublish("/finance", "1")
      expectMsg(PublishPayload("1", false))

      success
    }

    "check the same level" in {
      //“/finance” matches “/+”"
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("/+", self)
      bus ! BusPublish("/finance", "1")
      expectMsg(PublishPayload("1", false))

      success
    }

    "chech different levels" in {
      //“/finance” does not match “+”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("+", self)
      bus ! BusPublish("/finance", "1")
      expectNoMsg(Bag.wait_time)

      success
    }

    "check both plus and square" in {
      //“+/tennis/#” is valid
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("+/tennis/#", self)
      bus ! BusPublish("sport/tennis/values", "1")
      expectMsg(PublishPayload("1", false))

      bus ! BusPublish("sellings/tennis/summer", "2")
      expectMsg(PublishPayload("2", false))

      success
    }

    "sport+” is not valid" in {

      MqttTopicClassificator.checkTopicName("sport+") must throwA[BadSubscriptionException]
    }

    "check plus in a middle" in {
      //“sport/+/player1” is valid
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/+/player1", self)
      bus ! BusPublish("sport/tennis/player1", "1")
      expectMsg(PublishPayload("1", false))

      bus ! BusPublish("sport/boxing/player1", "2")
      expectMsg(PublishPayload("2", false))

      success
    }

    "retain message should be stored in a topic" in {
      val bus = system.actorOf(Props[EventBusActor])

      bus ! BusSubscribe("game/score", self)
      expectNoMsg(Bag.wait_time)

      bus ! BusUnsubscribe("game/score", self)

      bus ! BusPublish("game/score", "1", true)

      bus ! BusSubscribe("game/score", self)
      expectMsg(PublishPayload("1", true))

      bus ! BusPublish("game/score", "2", true)
      expectMsg(PublishPayload("2", false))

      bus ! BusSubscribe("#", self)
      expectMsg(PublishPayload("2", true))

      success

    }

    "delete retain message from topic with empty value" in {
      val bus = system.actorOf(Props[EventBusActor])

      bus ! BusSubscribe("game/score", self)
      expectNoMsg(Bag.wait_time)

      bus ! BusUnsubscribe("game/score", self)

      bus ! BusPublish("game/score", "1", true)

      bus ! BusSubscribe("game/score", self)
      expectMsg(PublishPayload("1", true))

      bus ! BusPublish("game/score", "2", true)
      expectMsg(PublishPayload("2", false))

      bus ! BusPublish("game/score", "3", true, true)
      expectMsg(PublishPayload("3", false))

      bus ! BusUnsubscribe("game/score", self)

      bus ! BusSubscribe("game/score", self)
      expectNoMsg(Bag.wait_time)

      bus ! BusSubscribe("#", self)
      expectNoMsg(Bag.wait_time)

      success

    }

    "complex scenario with square" in {
      val bus = system.actorOf(Props[EventBusActor])

      bus ! BusSubscribe("/#", self)
      expectNoMsg(Bag.wait_time)

      bus ! BusUnsubscribe("/Topic/C", self)
      expectNoMsg(Bag.wait_time)

      bus ! BusPublish("TopicA/B", "1", true)
      expectNoMsg(Bag.wait_time)

      bus ! BusPublish("TopicA/B", "2", false)
      expectNoMsg(Bag.wait_time)

      bus ! BusPublish("Topic/C", "3", true)
      expectNoMsg(Bag.wait_time)

      bus ! BusPublish("TopicA", "4", false)
      expectNoMsg(Bag.wait_time)

      bus ! BusPublish("/TopicA", "5", false)
      expectMsg(PublishPayload("5", false))

      bus ! BusSubscribe("Topic/C", self)
      expectMsg(PublishPayload("3", true))

      bus ! BusPublish("/TopicA", "6", true)
      expectMsg(PublishPayload("6", false))

      bus ! BusPublish("TopicA/C", "7", true)
      expectNoMsg(Bag.wait_time)

      bus ! BusPublish("/TopicA", "8", true)
      expectMsg(PublishPayload("8", false))

      success
    }
  }
}

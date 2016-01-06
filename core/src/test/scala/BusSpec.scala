package net.jetmq.tests

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.broker._
import org.specs2.mutable._
import org.specs2.specification.Scope

class BusSpec extends TestKit(ActorSystem("BusSpec")) with ImplicitSender with SpecificationLike with Scope {

  isolated

  "MqttEventBus handler actor" should {

    "simple pubsub" in {

      val bus = system.actorOf(Props[EventBusActor])

      bus ! Bus.Subscribe("greetings", self)
      bus ! Bus.Publish("time", "123")
      bus ! Bus.Publish("greetings", "hello")

      expectMsg(Bus.PublishPayload("hello", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "4.7.1.2 Multi-level wildcard" in {

      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("sport/tennis/player1/#", self)
      bus ! Bus.Publish("sport/tennis/player1", "1")
      expectMsg(Bus.PublishPayload("1", false))

      bus ! Bus.Publish("sport/tennis/player1/ranking", "2")
      expectMsg(Bus.PublishPayload("2", false))

      bus ! Bus.Publish("sport/tennis/player1/score/wimbledon", "3")
      expectMsg(Bus.PublishPayload("3", false))

      expectNoMsg(Bag.wait_time)
      success
    }


    "support multilevel only with a wildcard" in {

      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("sport/tennis/player2", self)
      bus ! Bus.Publish("sport/tennis/player2", "1")
      expectMsg(Bus.PublishPayload("1", false))

      bus ! Bus.Publish("sport/tennis/player2/ranking", "2")

      expectNoMsg(Bag.wait_time)
      success
    }

    "square for parent level" in {
      //“sport/#” also matches the singular “sport”, since # includes the parent level
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("sport/#", self)
      bus ! Bus.Publish("sport", "1")
      expectMsg(Bus.PublishPayload("1", false))

      expectNoMsg(Bag.wait_time)

      bus ! Bus.Subscribe("#", self)
      bus ! Bus.Publish("sport", "2")
      expectMsg(Bus.PublishPayload("2", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "square is root" in {
      //“#” is valid and will receive every Application Message
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("#", self)
      bus ! Bus.Publish("sport", "1")
      expectMsg(Bus.PublishPayload("1", false))

      bus ! Bus.Publish("dw", "2")
      expectMsg(Bus.PublishPayload("2", false))

      bus ! Bus.Publish("dw/1/2/3/values", "3")
      expectMsg(Bus.PublishPayload("3", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "work for valid input" in {
      //“sport/tennis/#” is valid

      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("sport/tennis/#", self)
      bus ! Bus.Publish("sport/tennis/123", "1")
      expectMsg(Bus.PublishPayload("1", false))

      expectNoMsg(Bag.wait_time)
      success
    }

    "“sport/tennis#” is not valid" in {
      MqttTopicClassificator.checkTopicName("sport/tennis#") must throwA[Bus.BadSubscriptionException]
    }

    "“sport/tennis/#/ranking”" in {

      MqttTopicClassificator.checkTopicName("sport/tennis/#/ranking") must throwA[Bus.BadSubscriptionException]
    }

    "check only one level for plus" in {
      //“sport/tennis/+” matches “sport/tennis/player1” and “sport/tennis/player2”, but not “sport/tennis/player1/ranking”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("sport/tennis/+", self)
      bus ! Bus.Publish("sport/tennis/player1", "1")
      expectMsg(Bus.PublishPayload("1", false))

      bus ! Bus.Publish("sport/tennis/player2", "2")
      expectMsg(Bus.PublishPayload("2", false))

      bus ! Bus.Publish("sport/tennis/player1/ranking", "3")

      expectNoMsg(Bag.wait_time)
      success
    }

    "check no parent level for plus" in {
      //“sport/+” does not match “sport” but it does match “sport/”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("sport/+", self)
      bus ! Bus.Publish("sport", "1")
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Publish("sport/", "2")
      expectMsg(Bus.PublishPayload("2", false))

      success
    }

    "check level for double plus" in {
      //“/finance” matches “+/+”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("+/+", self)
      bus ! Bus.Publish("/finance", "1")
      expectMsg(Bus.PublishPayload("1", false))

      success
    }

    "check the same level" in {
      //“/finance” matches “/+”"
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("/+", self)
      bus ! Bus.Publish("/finance", "1")
      expectMsg(Bus.PublishPayload("1", false))

      success
    }

    "chech different levels" in {
      //“/finance” does not match “+”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("+", self)
      bus ! Bus.Publish("/finance", "1")
      expectNoMsg(Bag.wait_time)

      success
    }

    "check both plus and square" in {
      //“+/tennis/#” is valid
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("+/tennis/#", self)
      bus ! Bus.Publish("sport/tennis/values", "1")
      expectMsg(Bus.PublishPayload("1", false))

      bus ! Bus.Publish("sellings/tennis/summer", "2")
      expectMsg(Bus.PublishPayload("2", false))

      success
    }

    "sport+” is not valid" in {

      MqttTopicClassificator.checkTopicName("sport+") must throwA[Bus.BadSubscriptionException]
    }

    "check plus in a middle" in {
      //“sport/+/player1” is valid
      val bus = system.actorOf(Props[EventBusActor])
      bus ! Bus.Subscribe("sport/+/player1", self)
      bus ! Bus.Publish("sport/tennis/player1", "1")
      expectMsg(Bus.PublishPayload("1", false))

      bus ! Bus.Publish("sport/boxing/player1", "2")
      expectMsg(Bus.PublishPayload("2", false))

      success
    }

    "retain message should be stored in a topic" in {
      val bus = system.actorOf(Props[EventBusActor])

      bus ! Bus.Subscribe("game/score", self)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Unsubscribe("game/score", self)

      bus ! Bus.Publish("game/score", "1", true)

      bus ! Bus.Subscribe("game/score", self)
      expectMsg(Bus.PublishPayload("1", true))

      bus ! Bus.Publish("game/score", "2", true)
      expectMsg(Bus.PublishPayload("2", false))

      bus ! Bus.Subscribe("#", self)
      expectMsg(Bus.PublishPayload("2", true))

      success

    }

    "delete retain message from topic with empty value" in {
      val bus = system.actorOf(Props[EventBusActor])

      bus ! Bus.Subscribe("game/score", self)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Unsubscribe("game/score", self)

      bus ! Bus.Publish("game/score", "1", true)

      bus ! Bus.Subscribe("game/score", self)
      expectMsg(Bus.PublishPayload("1", true))

      bus ! Bus.Publish("game/score", "2", true)
      expectMsg(Bus.PublishPayload("2", false))

      bus ! Bus.Publish("game/score", "3", true, true)
      expectMsg(Bus.PublishPayload("3", false))

      bus ! Bus.Unsubscribe("game/score", self)

      bus ! Bus.Subscribe("game/score", self)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Subscribe("#", self)
      expectNoMsg(Bag.wait_time)

      success

    }

    "complex scenario with square" in {
      val bus = system.actorOf(Props[EventBusActor])

      bus ! Bus.Subscribe("/#", self)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Unsubscribe("/Topic/C", self)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Publish("TopicA/B", "1", true)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Publish("TopicA/B", "2", false)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Publish("Topic/C", "3", true)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Publish("TopicA", "4", false)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Publish("/TopicA", "5", false)
      expectMsg(Bus.PublishPayload("5", false))

      bus ! Bus.Subscribe("Topic/C", self)
      expectMsg(Bus.PublishPayload("3", true))

      bus ! Bus.Publish("/TopicA", "6", true)
      expectMsg(Bus.PublishPayload("6", false))

      bus ! Bus.Publish("TopicA/C", "7", true)
      expectNoMsg(Bag.wait_time)

      bus ! Bus.Publish("/TopicA", "8", true)
      expectMsg(Bus.PublishPayload("8", false))

      success
    }
  }
}

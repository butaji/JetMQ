package net.jetmq.broker

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
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

      expectMsg("hello")

      expectNoMsg()
      success
    }

    "4.7.1.2 Multi-level wildcard" in {

      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/tennis/player1/#", self)
      bus ! BusPublish("sport/tennis/player1", "1")
      expectMsg("1")

      bus ! BusPublish("sport/tennis/player1/ranking", "2")
      expectMsg("2")

      bus ! BusPublish("sport/tennis/player1/score/wimbledon", "3")
      expectMsg("3")

      expectNoMsg()
      success
    }


    "support multilevel only with a wildcard" in {

      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/tennis/player2", self)
      bus ! BusPublish("sport/tennis/player2", "1")
      expectMsg("1")

      bus ! BusPublish("sport/tennis/player2/ranking", "2")

      expectNoMsg()
      success
    }

    "square for parent level" in {
      //“sport/#” also matches the singular “sport”, since # includes the parent level
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/#", self)
      bus ! BusPublish("sport", "1")
      expectMsg("1")

      expectNoMsg()

      bus ! BusSubscribe("#", self)
      bus ! BusPublish("sport", "2")
      expectMsg("2")

      expectNoMsg()
      success
    }

    "square is root" in {
      //“#” is valid and will receive every Application Message
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("#", self)
      bus ! BusPublish("sport", "1")
      expectMsg("1")

      bus ! BusPublish("dw", "2")
      expectMsg("2")

      bus ! BusPublish("dw/1/2/3/values", "3")
      expectMsg("3")

      expectNoMsg()
      success
    }

    "work for valid input" in {
      //“sport/tennis/#” is valid

      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/tennis/#", self)
      bus ! BusPublish("sport/tennis/123", "1")
      expectMsg("1")

      expectNoMsg()
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
      expectMsg("1")

      bus ! BusPublish("sport/tennis/player2", "2")
      expectMsg("2")

      bus ! BusPublish("sport/tennis/player1/ranking", "3")

      expectNoMsg()
      success
    }

    "check no parent level for plus" in {
      //“sport/+” does not match “sport” but it does match “sport/”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("sport/+", self)
      bus ! BusPublish("sport", "1")
      expectNoMsg()

      bus ! BusPublish("sport/", "2")
      expectMsg("2")

      success
    }

    "check level for double plus" in {
      //“/finance” matches “+/+”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("+/+", self)
      bus ! BusPublish("/finance", "1")
      expectMsg("1")

      success
    }

    "check the same level" in {
      //“/finance” matches “/+”"
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("/+", self)
      bus ! BusPublish("/finance", "1")
      expectMsg("1")

      success
    }

    "chech different levels" in {
      //“/finance” does not match “+”
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("+", self)
      bus ! BusPublish("/finance", "1")
      expectNoMsg()

      success
    }

    "check both plus and square" in {
      //“+/tennis/#” is valid
      val bus = system.actorOf(Props[EventBusActor])
      bus ! BusSubscribe("+/tennis/#", self)
      bus ! BusPublish("sport/tennis/values", "1")
      expectMsg("1")

      bus ! BusPublish("sellings/tennis/summer", "2")
      expectMsg("2")

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
      expectMsg("1")

      bus ! BusPublish("sport/boxing/player1", "2")
      expectMsg("2")

      success
    }

    "retain message should be stored in a topic" in {
      val bus = system.actorOf(Props[EventBusActor])

      bus ! BusSubscribe("game/score", self)
      expectNoMsg()

      bus ! BusUnsubscribe("game/score", self)

      bus ! BusPublish("game/score", "1", true)

      bus ! BusSubscribe("game/score", self)
      expectMsg("1")

      bus ! BusPublish("game/score", "2", true)

      bus ! BusSubscribe("#", self)
      expectMsg("2")
      expectMsg("2")

      success

    }
  }
}

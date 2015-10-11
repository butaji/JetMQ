package net.jetmq.broker

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.specs2.mutable._
import org.specs2.specification.Scope

class BusSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  isolated

  "MqttEventBus handler actor" should {

    "simple pubsub" in {

      val bus = new MqttEventBus
      bus.subscribe(self, "greetings")
      bus.publish(MsgEnvelope("time", "123"))
      bus.publish(MsgEnvelope("greetings", "hello"))
      expectMsg("hello")

      expectNoMsg()
      success
    }

    "4.7.1.2 Multi-level wildcard" in {

      val bus = new MqttEventBus
      bus.subscribe(self, "sport/tennis/player1/#")
      bus.publish(MsgEnvelope("sport/tennis/player1", "1"))
      expectMsg("1")

      bus.publish(MsgEnvelope("sport/tennis/player1/ranking", "2"))
      expectMsg("2")

      bus.publish(MsgEnvelope("sport/tennis/player1/score/wimbledon", "3"))
      expectMsg("3")

      expectNoMsg()
      success
    }


    "support multilevel only with a wildcard" in {

      val bus = new MqttEventBus
      bus.subscribe(self, "sport/tennis/player2")
      bus.publish(MsgEnvelope("sport/tennis/player2", "1"))
      expectMsg("1")

      bus.publish(MsgEnvelope("sport/tennis/player2/ranking", "2"))

      expectNoMsg()
      success
    }

    "“sport/#” also matches the singular “sport”, since # includes the parent level" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport/#")
      bus.publish(MsgEnvelope("sport", "1"))
      expectMsg("1")

      expectNoMsg()

      bus.subscribe(self, "#")
      bus.publish(MsgEnvelope("sport", "2"))
      expectMsg("2")

      expectNoMsg()
      success
    }

    "“#” is valid and will receive every Application Message" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "#")
      bus.publish(MsgEnvelope("sport", "1"))
      expectMsg("1")

      bus.publish(MsgEnvelope("dw", "2"))
      expectMsg("2")

      bus.publish(MsgEnvelope("dw/1/2/3/values", "3"))
      expectMsg("3")

      expectNoMsg()
      success
    }

    "“sport/tennis/#” is valid" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport/tennis/#")
      bus.publish(MsgEnvelope("sport/tennis/123", "1"))
      expectMsg("1")

      expectNoMsg()
      success
    }

    "“sport/tennis#” is not valid" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport/tennis#") must throwA[BadSubscriptionException]
    }

    "“sport/tennis/#/ranking”" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport/tennis/#/ranking") must throwA[BadSubscriptionException]
    }

    "“sport/tennis/+” matches “sport/tennis/player1” and “sport/tennis/player2”, but not “sport/tennis/player1/ranking”" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport/tennis/+")
      bus.publish(MsgEnvelope("sport/tennis/player1", "1"))
      expectMsg("1")

      bus.publish(MsgEnvelope("sport/tennis/player2", "2"))
      expectMsg("2")

      bus.publish(MsgEnvelope("sport/tennis/player1/ranking", "3"))

      expectNoMsg()
      success
    }

    "“sport/+” does not match “sport” but it does match “sport/”" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport/+")
      bus.publish(MsgEnvelope("sport", "1"))
      expectNoMsg()

      bus.publish(MsgEnvelope("sport/", "2"))
      expectMsg("2")

      success
    }

    "“/finance” matches “+/+”" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "+/+")
      bus.publish(MsgEnvelope("/finance", "1"))
      expectMsg("1")

      success
    }

    "“/finance” matches “/+”" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "/+")
      bus.publish(MsgEnvelope("/finance", "1"))
      expectMsg("1")

      success
    }

    "“/finance” does not match “+”" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "+")
      bus.publish(MsgEnvelope("/finance", "1"))
      expectNoMsg()

      success
    }

    "“+/tennis/#” is valid" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "+/tennis/#")
      bus.publish(MsgEnvelope("sport/tennis/values", "1"))
      expectMsg("1")

      bus.publish(MsgEnvelope("sellings/tennis/summer", "2"))
      expectMsg("2")

      success
    }

    "sport+” is not valid" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport+") must throwA[BadSubscriptionException]
    }

    "“sport/+/player1” is valid" in {
      val bus = new MqttEventBus
      bus.subscribe(self, "sport/+/player1")
      bus.publish(MsgEnvelope("sport/tennis/player1", "1"))
      expectMsg("1")

      bus.publish(MsgEnvelope("port/boxing/player1", "2"))
      expectMsg("2")

      success
    }
  }
}

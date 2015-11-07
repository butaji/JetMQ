package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.Helpers._
import net.jetmq.SessionsManagerActor
import org.specs2.mutable._
import org.specs2.specification.Scope

class FullOfflineSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    def create_actor(name: String) = {
      system.actorOf(Props(new ConnectionActor(devices)), name)
    }

    "Scenario #53663" in {
      val h = create_actor("53663")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #53664" in {
      val h = create_actor("53664")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #53665" in {
      val h = create_actor("53665")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "8206000200012300".toTcpReceived //SUBSCRIBE
      expectMsg("9003000200".toTcpWrite) //SUBACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #53666" in {
      val h = create_actor("53666")

      h ! "101600044d51545404000000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "8208000200032b2f2b02".toTcpReceived //SUBSCRIBE
      expectMsg("9003000202".toTcpWrite) //SUBACK

      h ! "e000".toTcpReceived //DISCONNECT

      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #53667" in {
      val h = create_actor("53667")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "300f0008546f706963412f42716f732030".toTcpReceived //PUBLISH
      h ! "32100007546f7069632f430002716f732031".toTcpReceived //PUBLISH
      h ! "34110008546f706963412f430003716f732032".toTcpReceived //PUBLISH
      expectMsg("40020002".toTcpWrite) //PUBACK

      expectMsg("50020003".toTcpWrite) //PUBREC

      h ! "62020003".toTcpReceived //PUBREL
      expectMsg("70020003".toTcpWrite) //PUBCOMP

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #53668" in {
      val h = create_actor("53668")

      h ! "101600044d51545404000000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020100".toTcpWrite) //CONNACK

      expectMsg("3a100007546f7069632f430001716f732031".toTcpWrite) //PUBLISH

      expectMsg("3c110008546f706963412f430002716f732032".toTcpWrite) //PUBLISH

      h ! "40020001".toTcpReceived //PUBACK
      h ! "50020002".toTcpReceived //PUBREC
      expectMsg("62020002".toTcpWrite) //PUBREL

      h ! "70020002".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }


  }
}

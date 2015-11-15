package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.Helpers._
import net.jetmq.SessionsManagerActor
import org.specs2.mutable._
import org.specs2.specification.Scope

class RetainedSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    def create_actor(name: String) = {
      system.actorOf(Props(new TcpConnectionActor(devices)).withMailbox("priority-dispatcher"), name)
    }

    "Scenario #50692" in {
      val h = create_actor("50692")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50693" in {
      val h = create_actor("50693")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50694" in {
      val h = create_actor("50694")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "8206000200012300".toTcpReceived //SUBSCRIBE
      expectMsg("9003000200".toTcpWrite) //SUBACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50696" in {
      val h = create_actor("50696")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "310f0008546f706963412f42716f732030".toTcpReceived //PUBLISH

      h ! "33100007546f7069632f430002716f732031".toTcpReceived //PUBLISH
      expectMsg("40020002".toTcpWrite) //PUBACK

      h ! "35110008546f706963412f430003716f732032".toTcpReceived //PUBLISH
      expectMsg("50020003".toTcpWrite) //PUBREC
      h ! "62020003".toTcpReceived //PUBREL
      expectMsg("70020003".toTcpWrite) //PUBCOMP

      h ! "8208000400032b2f2b02".toTcpReceived //SUBSCRIBE
      expectMsg("9003000402".toTcpWrite) //SUBACK

      expectMsg("33100007546f7069632f430001716f732031".toTcpWrite) //PUBLISH
      h ! "40020001".toTcpReceived //PUBACK

      expectMsg("310f0008546f706963412f42716f732030".toTcpWrite) //PUBLISH

      expectMsg("35110008546f706963412f430002716f732032".toTcpWrite) //PUBLISH
      h ! "50020002".toTcpReceived //PUBREC

      expectMsg("62020002".toTcpWrite) //PUBREL
      h ! "70020002".toTcpReceived //PUBCOMP

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50697" in {
      val h = create_actor("50697")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "310a0008546f706963412f42".toTcpReceived //PUBLISH
      h ! "330b0007546f7069632f430005".toTcpReceived //PUBLISH
      h ! "350c0008546f706963412f430006".toTcpReceived //PUBLISH
      expectMsg("40020005".toTcpWrite) //PUBACK

      expectMsg("50020006".toTcpWrite) //PUBREC

      h ! "62020006".toTcpReceived //PUBREL
      expectMsg("70020006".toTcpWrite) //PUBCOMP

      h ! "8208000700032b2f2b02".toTcpReceived //SUBSCRIBE
      expectMsg("9003000702".toTcpWrite) //SUBACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

  }
}

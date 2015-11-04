package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.Helpers._
import net.jetmq.SessionsManagerActor
import org.specs2.mutable._
import org.specs2.specification.Scope

class FullBasicSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    def create_actor(name: String) = {
      system.actorOf(Props(new ConnectionActor(devices)), name)
    }

    "Scenario #50856" in {
      val h = create_actor("50856")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg()
      success
    }

    "Scenario #50857" in {
      val h = create_actor("50857")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg()
      success
    }

    "Scenario #50858" in {
      val h = create_actor("50858")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "8206000200012300".toTcpReceived //SUBSCRIBE
      expectMsg("9003000200".toTcpWrite) //SUBACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg()
      success
    }

    "Scenario #50859" in {
      val h = create_actor("50859")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg()
      success
    }

    "Scenario #50860" in {
      val h = create_actor("50860")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "820b00020006546f7069634102".toTcpReceived //SUBSCRIBE
      expectMsg("9003000202".toTcpWrite) //SUBACK

      h ! "300d0006546f70696341716f732030".toTcpReceived //PUBLISH
      expectMsg("300d0006546f70696341716f732030".toTcpWrite) //PUBLISH

      h ! "320f0006546f706963410003716f732031".toTcpReceived //PUBLISH
      expectMsg("40020003".toTcpWrite) //PUBACK

      h ! "340f0006546f706963410004716f732032".toTcpReceived //PUBLISH

      expectMsg("320f0006546f706963410001716f732031".toTcpWrite) //PUBLISH

      h ! "40020001".toTcpReceived //PUBACK
      expectMsg("50020004".toTcpWrite) //PUBREC

      h ! "62020004".toTcpReceived //PUBREL
      expectMsg("340f0006546f706963410002716f732032".toTcpWrite) //PUBLISH

      expectMsg("70020004".toTcpWrite) //PUBCOMP

      h ! "50020002".toTcpReceived //PUBREC
      expectMsg("62020002".toTcpWrite) //PUBREL

      h ! "70020002".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg()
      success
    }

    "Scenario #50861" in {
      val h = create_actor("50861")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg(Tcp.Close)
      expectNoMsg()
      success
    }

    "Scenario #50862" in {
      val h = create_actor("50862")

      h ! "10140002686a04020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg(Tcp.Close)
      expectNoMsg()
      success
    }
  }
}

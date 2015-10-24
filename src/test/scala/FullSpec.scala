package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.SessionsManagerActor
import net.jetmq.Helpers._
import org.specs2.mutable._
import org.specs2.specification.Scope

class FullSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    def create_actor() = {
      val coder = system.actorOf(Props[PacketsActor])
      system.actorOf(Props(new ConnectionActor(devices, coder)))
    }

    "Scenario #52297" in {
      val h = create_actor

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52298" in {
      val h = create_actor

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52299" in {
      val h = create_actor

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "8206000200012300".toTcpReceived //SUBSCRIBE
      expectMsg("9003000200".toTcpWrite) //SUBACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52300" in {
      val h = create_actor

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52301" in {
      val h = create_actor

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "820b00020006546f7069634102".toTcpReceived //SUBSCRIBE
      expectMsg("9003000202".toTcpWrite) //SUBACK

      h ! "300d0006546f70696341716f732030".toTcpReceived //PUBLISH

      h ! "320f0006546f706963410003716f732031".toTcpReceived //PUBLISH
      expectMsg("40020003".toTcpWrite) //PUBACK

      h ! "340f0006546f706963410004716f732032".toTcpReceived //PUBLISH
      expectMsg("50020004".toTcpWrite) //PUBREC

      expectMsg("300d0006546f70696341716f732030".toTcpWrite) //PUBLISH

      expectMsg("320f0006546f706963410001716f732031".toTcpWrite) //PUBLISH

      h ! "40020001".toTcpReceived //PUBACK

      h ! "62020004".toTcpReceived //PUBREL
      expectMsg("340f0006546f706963410002716f732032".toTcpWrite) //PUBLISH

      expectMsg("70020004".toTcpWrite) //PUBCOMP

      h ! "50020002".toTcpReceived //PUBREC
      expectMsg("62020002".toTcpWrite) //PUBREL

      h ! "70020002".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52302" in {
      val h = create_actor

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectNoMsg()
      success
    }

    "Scenario #52303" in {
      val h = create_actor

      h ! "10140002686a04020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectNoMsg()
      success
    }

    "Scenario #52304" in {
      val h = create_actor

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "310f0008546f706963412f42716f732030".toTcpReceived //PUBLISH
      h ! "33100007546f7069632f430005716f732031".toTcpReceived //PUBLISH
      h ! "35110008546f706963412f430006716f732032".toTcpReceived //PUBLISH
      expectMsg("40020005".toTcpWrite) //PUBACK

      expectMsg("50020006".toTcpWrite) //PUBREC

      h ! "62020006".toTcpReceived //PUBREL
      expectMsg("70020006".toTcpWrite) //PUBCOMP

      h ! "8208000700032b2f2b02".toTcpReceived //SUBSCRIBE
      expectMsg("9003000702".toTcpWrite) //SUBACK

      expectMsg("33100007546f7069632f430001716f732031".toTcpWrite) //PUBLISH

      h ! "40020001".toTcpReceived //PUBACK
      expectMsg("310f0008546f706963412f42716f732030".toTcpWrite) //PUBLISH

      expectMsg("35110008546f706963412f430002716f732032".toTcpWrite) //PUBLISH


      h ! "50020002".toTcpReceived //PUBREC
      expectMsg("62020002".toTcpWrite) //PUBREL

      h ! "70020002".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52307" in {
      val h = create_actor

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "310a0008546f706963412f42".toTcpReceived //PUBLISH

      h ! "330b0007546f7069632f430008".toTcpReceived //PUBLISH
      expectMsg("40020008".toTcpWrite) //PUBACK

      h ! "350c0008546f706963412f430009".toTcpReceived //PUBLISH
      expectMsg("50020009".toTcpWrite) //PUBREC

      h ! "62020009".toTcpReceived //PUBREL
      expectMsg("70020009".toTcpWrite) //PUBCOMP

      h ! "8208000a00032b2f2b02".toTcpReceived //SUBSCRIBE
      expectMsg("9003000a02".toTcpWrite) //SUBACK

      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52308" in {
      val h = create_actor

      h ! "101600044d51545404000000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "8208000b00032b2f2b02".toTcpReceived //SUBSCRIBE
      expectMsg("9003000b02".toTcpWrite) //SUBACK
      h ! "e000".toTcpReceived //DISCONNECT

      expectNoMsg()
      success
    }

    "Scenario #52309" in {
      val h = create_actor

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
      expectNoMsg()
      success
    }

    "Scenario #52312" in {
      val h = create_actor

      h ! "101600044d51545404000000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020100".toTcpWrite) //CONNACK

      expectMsg("3a100007546f7069632f430001716f732031".toTcpWrite) //PUBLISH

      expectMsg("3c110008546f706963412f430002716f732032".toTcpWrite) //PUBLISH

      h ! "40020001".toTcpReceived //PUBACK
      h ! "50020002".toTcpReceived //PUBREC
      expectMsg("62020002".toTcpWrite) //PUBREL

      h ! "70020002".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52313" in {
      val h = create_actor

      h ! "103800044d51545404160002000a6d79636c69656e7469640007546f7069632f430017636c69656e74206e6f7420646973636f6e6e6563746564".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      expectNoMsg()
      success
    }

    "Scenario #52314" in {
      val h = create_actor

      h ! "101700044d51545404000000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "820c00040007546f7069632f4302".toTcpReceived //SUBSCRIBE
      expectMsg("9003000402".toTcpWrite) //SUBACK

      expectMsg("34220007546f7069632f430001636c69656e74206e6f7420646973636f6e6e6563746564".toTcpWrite) //PUBLISH

      h ! "50020001".toTcpReceived //PUBREC
      expectMsg("62020001".toTcpWrite) //PUBREL

      h ! "70020001".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52315" in {
      val h = create_actor

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "8218000c0008546f706963412f23020008546f706963412f2b01".toTcpReceived //SUBSCRIBE
      h ! "34250008546f706963412f43000d6f7665726c617070696e6720746f7069632066696c74657273".toTcpReceived //PUBLISH
      expectMsg("9004000c0201".toTcpWrite) //SUBACK

      expectMsg("5002000d".toTcpWrite) //PUBREC

      h ! "6202000d".toTcpReceived //PUBREL
      expectMsg("34250008546f706963412f4300016f7665726c617070696e6720746f7069632066696c74657273".toTcpWrite) //PUBLISH

      expectMsg("7002000d".toTcpWrite) //PUBCOMP

      h ! "50020001".toTcpReceived //PUBREC
      expectMsg("62020001".toTcpWrite) //PUBREL

      h ! "70020001".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52317" in {
      val h = create_actor

      h ! "103100044d51545404160005000a6d79636c69656e74696400072f546f7069634100106b656570616c69766520657870697279".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      expectNoMsg()
      success
    }

    "Scenario #52318" in {
      val h = create_actor

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "820c000500072f546f7069634102".toTcpReceived //SUBSCRIBE
      expectMsg("9003000502".toTcpWrite) //SUBACK

      expectMsg("341b00072f546f7069634100016b656570616c69766520657870697279".toTcpWrite) //PUBLISH

      h ! "50020001".toTcpReceived //PUBREC
      expectMsg("62020001".toTcpWrite) //PUBREL

      h ! "70020001".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52325" in {
      val h = create_actor

      h ! "101700044d51545404000000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020000".toTcpWrite) //CONNACK

      h ! "820d00060008546f706963412f2302".toTcpReceived //SUBSCRIBE
      h ! "320c0008546f706963412f420007".toTcpReceived //PUBLISH
      h ! "340c0008546f706963412f430008".toTcpReceived //PUBLISH
      expectMsg("9003000602".toTcpWrite) //SUBACK

      expectMsg("320c0008546f706963412f420001".toTcpWrite) //PUBLISH

      expectMsg("40020007".toTcpWrite) //PUBACK

      expectMsg("50020008".toTcpWrite) //PUBREC

      h ! "62020008".toTcpReceived //PUBREL
      expectMsg("340c0008546f706963412f430002".toTcpWrite) //PUBLISH

      expectMsg("70020008".toTcpWrite) //PUBCOMP

      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }

    "Scenario #52326" in {
      val h = create_actor

      h ! "101700044d51545404000000000b6d79636c69656e74696432".toTcpReceived //CONNECT
      expectMsg("20020100".toTcpWrite) //CONNACK

      expectMsg("3a0c0008546f706963412f420001".toTcpWrite) //PUBLISH

      expectMsg("3c0c0008546f706963412f430002".toTcpWrite) //PUBLISH

      h ! "40020001".toTcpReceived //PUBACK
      h ! "50020002".toTcpReceived //PUBREC
      expectMsg("62020002".toTcpWrite) //PUBREL

      h ! "70020002".toTcpReceived //PUBCOMP
      h ! "e000".toTcpReceived //DISCONNECT
      expectNoMsg()
      success
    }




  }
}

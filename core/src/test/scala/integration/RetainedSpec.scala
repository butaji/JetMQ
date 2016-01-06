package integration

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.Tcp
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import net.jetmq.broker._
import Helpers._
import net.jetmq.tests.Bag
import org.specs2.mutable._
import org.specs2.specification.Scope

class RetainedSpec extends TestKit(ActorSystem("RetainedSpec")) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    implicit val materializer = ActorMaterializer()(system)

    def create_actor(name: String): ActorRef = {
      val h = system.actorOf(Props(new TcpConnectionActor(devices)), name)

      val s = Source(ActorPublisher[ByteString](h))
      s.to(Sink.actorRef(self, Tcp.Close)).run()

      return h
    }

    "Scenario #50692" in {
      val h = create_actor("50692")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //CONNECT
      expectMsg("20020000".toByteString) //CONNACK

      h ! "e000".toByteString //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50693" in {
      val h = create_actor("50693")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toByteString //CONNECT
      expectMsg("20020000".toByteString) //CONNACK

      h ! "e000".toByteString //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50694" in {
      val h = create_actor("50694")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //CONNECT
      expectMsg("20020000".toByteString) //CONNACK

      h ! "8206000200012300".toByteString //SUBSCRIBE
      expectMsg("9003000200".toByteString) //SUBACK

      h ! "e000".toByteString //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50696" in {
      val h = create_actor("50696")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //CONNECT
      expectMsg("20020000".toByteString) //CONNACK

      h ! "310f0008546f706963412f42716f732030".toByteString //PUBLISH

      h ! "33100007546f7069632f430002716f732031".toByteString //PUBLISH
      expectMsg("40020002".toByteString) //PUBACK

      h ! "35110008546f706963412f430003716f732032".toByteString //PUBLISH
      expectMsg("50020003".toByteString) //PUBREC
      h ! "62020003".toByteString //PUBREL
      expectMsg("70020003".toByteString) //PUBCOMP

      h ! "8208000400032b2f2b02".toByteString //SUBSCRIBE
      expectMsg("9003000402".toByteString) //SUBACK

      expectMsg("33100007546f7069632f430001716f732031".toByteString) //PUBLISH
      h ! "40020001".toByteString //PUBACK

      expectMsg("310f0008546f706963412f42716f732030".toByteString) //PUBLISH

      expectMsg("35110008546f706963412f430002716f732032".toByteString) //PUBLISH
      h ! "50020002".toByteString //PUBREC

      expectMsg("62020002".toByteString) //PUBREL
      h ! "70020002".toByteString //PUBCOMP

      h ! "e000".toByteString //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario #50697" in {
      val h = create_actor("50697")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //CONNECT
      expectMsg("20020000".toByteString) //CONNACK

      h ! "310a0008546f706963412f42".toByteString //PUBLISH
      h ! "330b0007546f7069632f430005".toByteString //PUBLISH
      h ! "350c0008546f706963412f430006".toByteString //PUBLISH
      expectMsg("40020005".toByteString) //PUBACK

      expectMsg("50020006".toByteString) //PUBREC

      h ! "62020006".toByteString //PUBREL
      expectMsg("70020006".toByteString) //PUBCOMP

      h ! "8208000700032b2f2b02".toByteString //SUBSCRIBE
      expectMsg("9003000702".toByteString) //SUBACK

      h ! "e000".toByteString //DISCONNECT
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

  }
}

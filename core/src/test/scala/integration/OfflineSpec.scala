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

class OfflineSpec extends TestKit(ActorSystem("OfflineSpec")) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    implicit val materializer = ActorMaterializer()(system)

    def create_actor(name: String): ActorRef = {
      val h = system.actorOf(Props(new TcpConnectionActor(devices)).withMailbox("priority-dispatcher"), name)

      val s = Source(ActorPublisher[ByteString](h))
      s.to(Sink.actorRef(self, Tcp.Close)).run()

      return h
    }

    "Scenario 53663" in {
      val h = create_actor("53663")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53664" in {
      val h = create_actor("53664")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53665" in {
      val h = create_actor("53665")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53666" in {
      val h = create_actor("53666")

      h ! "101600044d51545404000000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8208000200032b2f2b02".toByteString //Subscribe(Header(false,1,false),2,Vector((+/+,2)))

      expectMsg("9003000202".toByteString) //Suback(Header(false,0,false),2,Vector(2))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53667" in {
      val h = create_actor("53667")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "300f0008546f706963412f42716f732030".toByteString //Publish(Header(false,0,false),TopicA/B,0,ByteVector(5 bytes, 0x716f732030))

      h ! "32100007546f7069632f430002716f732031".toByteString //Publish(Header(false,1,false),Topic/C,2,ByteVector(5 bytes, 0x716f732031))

      h ! "34110008546f706963412f430003716f732032".toByteString //Publish(Header(false,2,false),TopicA/C,3,ByteVector(5 bytes, 0x716f732032))
      expectMsg("40020002".toByteString) //Puback(Header(false,0,false),2)
      expectMsg("50020003".toByteString) //Pubrec(Header(false,0,false),3)

      h ! "62020003".toByteString //Pubrel(Header(false,1,false),3)
      expectMsg("70020003".toByteString) //Pubcomp(Header(false,0,false),3)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53668" in {
      val h = create_actor("53668")

      h ! "101600044d51545404000000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,0),myclientid,None,None,None,None)
      expectMsg("20020100".toByteString) //Connack(Header(false,0,false),256)

      expectMsg("3a100007546f7069632f430001716f732031".toByteString) //Publish(Header(true,1,false),Topic/C,1,ByteVector(5 bytes, 0x716f732031))

      expectMsg("3c110008546f706963412f430002716f732032".toByteString) //Publish(Header(true,2,false),TopicA/C,2,ByteVector(5 bytes, 0x716f732032))

      h ! "40020001".toByteString //Puback(Header(false,0,false),1)

      h ! "50020002".toByteString //Pubrec(Header(false,0,false),2)
      expectMsg("62020002".toByteString) //Pubrel(Header(false,1,false),2)

      h ! "70020002".toByteString //Pubcomp(Header(false,0,false),2)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}

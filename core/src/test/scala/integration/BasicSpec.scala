package integration

import akka.actor.Status.Failure
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

class BasicSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender with SpecificationLike with Scope {

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

    "Scenario 51208" in {
      val h = create_actor("51208")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51209" in {
      val h = create_actor("51209")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51210" in {
      val h = create_actor("51210")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51211" in {
      val h = create_actor("51211")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51212" in {
      val h = create_actor("51212")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "820b00020006546f7069634102".toByteString //Subscribe(Header(false,1,false),2,Vector((TopicA,2)))
      expectMsg("9003000202".toByteString) //Suback(Header(false,0,false),2,Vector(2))

      h ! "300d0006546f70696341716f732030320f0006546f706963410003716f732031340f0006546f706963410004716f732032".toByteString //Publish(Header(false,0,false),TopicA,0,ByteVector(5 bytes, 0x716f732030))
      expectMsg("40020003".toByteString) //Puback(Header(false,0,false),3)
      expectMsg("50020004".toByteString) //Pubrec
      expectMsg("300d0006546f70696341716f732030".toByteString) //Publish(Header(false,0,false),TopicA,0,ByteVector(5 bytes, 0x716f732030))
      expectMsg("320f0006546f706963410001716f732031".toByteString) //Publish(Header(false,1,false),TopicA,1,ByteVector(5 bytes, 0x716f732031))

      h ! "40020001".toByteString //Puback(Header(false,0,false),1)

      h ! "62020004".toByteString //Pubrel(Header(false,1,false),4)
      expectMsg("340f0006546f706963410002716f732032".toByteString) //Publish(Header(false,2,false),TopicA,2,ByteVector(5 bytes, 0x716f732032))
      expectMsg("70020004".toByteString) //Pubcomp(Header(false,0,false),4)

      h ! "50020002".toByteString //Pubrec(Header(false,0,false),2)
      expectMsg("62020002".toByteString) //Pubrel(Header(false,1,false),2)

      h ! "70020002".toByteString //Pubcomp(Header(false,0,false),2)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51213" in {
      val h = create_actor("51213")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51214" in {
      val h = create_actor("51214")

      h ! "10140002686a04020000000a6d79636c69656e746964".toByteString //Broken package
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}

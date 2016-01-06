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

class RedeliverySpec extends TestKit(ActorSystem("RedeliverySpec")) with ImplicitSender with SpecificationLike with Scope {
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

    "Scenario 60820" in {
      val h = create_actor("60820")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 60821" in {
      val h = create_actor("60821")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 60822" in {
      val h = create_actor("60822")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 60824" in {
      val h = create_actor("60824")

      h ! "101700044d51545404000000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "820d00020008546f706963412f2302".toByteString //Subscribe(Header(false,1,false),2,Vector((TopicA/#,2)))
      expectMsg("9003000202".toByteString) //Suback(Header(false,0,false),2,Vector(2))

      h ! "320c0008546f706963412f420003".toByteString //Publish(Header(false,1,false),TopicA/B,3,ByteVector(empty))
      expectMsg("40020003".toByteString) //Puback(Header(false,0,false),3)

      h ! "340c0008546f706963412f430004".toByteString //Publish(Header(false,2,false),TopicA/C,4,ByteVector(empty))
      Thread.sleep(100)

      expectMsg("50020004".toByteString) //Pubrec(Header(false,0,false),4)
      expectMsg("320c0008546f706963412f420001".toByteString) //Publish(Header(false,1,false),TopicA/B,1,ByteVector(empty))

      h ! "62020004".toByteString //Pubrel(Header(false,1,false),4)
      expectMsg("340c0008546f706963412f430002".toByteString) //Publish(Header(false,2,false),TopicA/C,2,ByteVector(empty))
      expectMsg("70020004".toByteString) //Pubcomp(Header(false,0,false),4)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 60825" in {
      val h = create_actor("60825")

      h ! "101700044d51545404000000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,0),myclientid2,None,None,None,None)
      expectMsg("20020100".toByteString) //Connack(Header(false,0,false),256)
      expectMsg("3a0c0008546f706963412f420001".toByteString) //Publish(Header(true,1,false),TopicA/B,1,ByteVector(empty))
      expectMsg("3c0c0008546f706963412f430002".toByteString) //Publish(Header(true,2,false),TopicA/C,2,ByteVector(empty))

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

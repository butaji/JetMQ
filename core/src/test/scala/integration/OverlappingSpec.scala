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

class OverlappingSpec extends TestKit(ActorSystem("OverlappingSpec")) with ImplicitSender with SpecificationLike with Scope {

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

    "Scenario 58618" in {
      val h = create_actor("58618")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 58619" in {
      val h = create_actor("58619")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 58620" in {
      val h = create_actor("58620")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 58621" in {
      val h = create_actor("58621")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "821800020008546f706963412f23020008546f706963412f2b01".toByteString //Subscribe(Header(false,1,false),2,Vector((TopicA/#,2), (TopicA/+,1)))
      expectMsg("900400020201".toByteString) //Suback(Header(false,0,false),2,Vector(2, 1))

      h ! "34250008546f706963412f4300036f7665726c617070696e6720746f7069632066696c74657273".toByteString //Publish(Header(false,2,false),TopicA/C,3,ByteVector(25 bytes, 0x6f7665726c617070696e6720746f7069632066696c74657273))
      Thread.sleep(500) //to granted order

      expectMsg("50020003".toByteString) //Pubrec(Header(false,0,false),3)

      h ! "62020003".toByteString //Pubrel(Header(false,1,false),3)

      expectMsg("34250008546f706963412f4300016f7665726c617070696e6720746f7069632066696c74657273".toByteString) //Publish(Header(false,2,false),TopicA/C,1,ByteVector(25 bytes, 0x6f7665726c617070696e6720746f7069632066696c74657273))

      expectMsg("70020003".toByteString) //Pubcomp(Header(false,0,false),3)


      h ! "50020001".toByteString //Pubrec(Header(false,0,false),1)
      expectMsg("62020001".toByteString) //Pubrel(Header(false,1,false),1)

      h ! "70020001".toByteString //Pubcomp(Header(false,0,false),1)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

  }
}

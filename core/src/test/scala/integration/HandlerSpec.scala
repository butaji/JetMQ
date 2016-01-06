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

class HandlerSpec extends TestKit(ActorSystem("HandlerSpec")) with ImplicitSender with SpecificationLike with Scope {

  sequential

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "sessions")

    implicit val materializer = ActorMaterializer()(system)

    def create_actor(name: String): ActorRef = {
      val h = system.actorOf(Props(new TcpConnectionActor(devices)), name)

      val s = Source(ActorPublisher[ByteString](h))
      s.to(Sink.actorRef(self, Tcp.Close)).run()

      return h
    }
    "Scenario 61115" in {
      val h = create_actor("61115")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61116" in {
      val h = create_actor("61116")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61117" in {
      val h = create_actor("61117")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61118" in {
      val h = create_actor("61118")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61119" in {
      val h = create_actor("61119")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61120" in {
      val h = create_actor("61120")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61122" in {
      val h = create_actor("61122")

      h ! "8208000100032b2f4302".toByteString //Subscribe(Header(false,1,false),1,Vector((+/C,2)))

      h ! "358c100008546f706963412f4200026c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,2,true),TopicA/B,2,ByteVector(2048 bytes, #259165444))
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61124" in {
      val h = create_actor("61124")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61125" in {
      val h = create_actor("61125")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61126" in {
      val h = create_actor("61126")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61127" in {
      val h = create_actor("61127")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61128" in {
      val h = create_actor("61128")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61129" in {
      val h = create_actor("61129")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61130" in {
      val h = create_actor("61130")

      h ! "102300044d5154540402003c00173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,60),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "328a100006546f7069634100016c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,1,false),TopicA,1,ByteVector(2048 bytes, #259165444))
      expectMsg("40020001".toByteString) //Puback(Header(false,0,false),1)

      h ! "104a00044d5154540400003c003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61131" in {
      val h = create_actor("61131")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61132" in {
      val h = create_actor("61132")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61133" in {
      val h = create_actor("61133")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61134" in {
      val h = create_actor("61134")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61135" in {
      val h = create_actor("61135")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61136" in {
      val h = create_actor("61136")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61137" in {
      val h = create_actor("61137")

      h ! "a20b000100072f546f70696341".toByteString //Unsubscribe(Header(false,1,false),1,Vector(/TopicA))

      h ! "820b00020006546f7069634100".toByteString //Subscribe(Header(false,1,false),2,Vector((TopicA,0)))
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61138" in {
      val h = create_actor("61138")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61139" in {
      val h = create_actor("61139")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61140" in {
      val h = create_actor("61140")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61141" in {
      val h = create_actor("61141")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61142" in {
      val h = create_actor("61142")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61143" in {
      val h = create_actor("61143")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61144" in {
      val h = create_actor("61144")

      h ! "104a00044d5154540402003c003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,60),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8207000100022f2b02".toByteString //Subscribe(Header(false,1,false),1,Vector((/+,2)))
      expectMsg("9003000102".toByteString) //Suback(Header(false,0,false),1,Vector(2))

      h ! "a207000200032b2f43".toByteString //Unsubscribe(Header(false,1,false),2,Vector(+/C))
      expectMsg("b0020002".toByteString) //Unsuback(Header(false,0,false),2)

      h ! "820c000300072f546f7069634102".toByteString //Subscribe(Header(false,1,false),3,Vector((/TopicA,2)))
      expectMsg("9003000302".toByteString) //Suback(Header(false,0,false),3,Vector(2))

      h ! "31080006546f70696341".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(empty))

      h ! "a2050004000123".toByteString //Unsubscribe(Header(false,1,false),4,Vector(#))
      expectMsg("b0020004".toByteString) //Unsuback(Header(false,0,false),4)

      h ! "a20a00050006546f70696341".toByteString //Unsubscribe(Header(false,1,false),5,Vector(TopicA))
      expectMsg("b0020005".toByteString) //Unsuback(Header(false,0,false),5)

      h ! "340a0006546f706963410006".toByteString //Publish(Header(false,2,false),TopicA,6,ByteVector(empty))
      expectMsg("50020006".toByteString) //Pubrec(Header(false,0,false),6)

      h ! "62020006".toByteString //Pubrel(Header(false,1,false),6)

      h ! "310d0008546f706963412f43333333".toByteString //Publish(Header(false,0,true),TopicA/C,0,ByteVector(3 bytes, 0x333333))
      expectMsg("70020006".toByteString) //Pubcomp(Header(false,0,false),6)

      h ! "330d0006546f706963410007333333".toByteString //Publish(Header(false,1,true),TopicA,7,ByteVector(3 bytes, 0x333333))
      expectMsg("40020007".toByteString) //Puback(Header(false,0,false),7)

      h ! "330c0008546f706963412f430008".toByteString //Publish(Header(false,1,true),TopicA/C,8,ByteVector(empty))
      expectMsg("40020008".toByteString) //Puback(Header(false,0,false),8)

      h ! "300a00072f546f7069634131".toByteString //Publish(Header(false,0,false),/TopicA,0,ByteVector(1 bytes, 0x31))
      expectMsg("300a00072f546f7069634131".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(1 bytes, 0x31))

      h ! "a206000900022f23".toByteString //Unsubscribe(Header(false,1,false),9,Vector(/#))
      expectMsg("b0020009".toByteString) //Unsuback(Header(false,0,false),9)

      h ! "328c100008546f706963412f42000a6c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,1,false),TopicA/B,10,ByteVector(2048 bytes, #259165444))
      expectMsg("4002000a".toByteString) //Puback(Header(false,0,false),10)

      h ! "a20c000b0008546f706963412f42".toByteString //Unsubscribe(Header(false,1,false),11,Vector(TopicA/B))
      expectMsg("b002000b".toByteString) //Unsuback(Header(false,0,false),11)

      h ! "340c0008546f706963412f42000c".toByteString //Publish(Header(false,2,false),TopicA/B,12,ByteVector(empty))
      expectMsg("5002000c".toByteString) //Pubrec(Header(false,0,false),12)

      h ! "6202000c".toByteString //Pubrel(Header(false,1,false),12)

      h ! "320a0006546f70696341000d".toByteString //Publish(Header(false,1,false),TopicA,13,ByteVector(empty))
      expectMsg("7002000c".toByteString) //Pubcomp(Header(false,0,false),12)
      expectMsg("4002000d".toByteString) //Puback(Header(false,0,false),13)

      h ! "320b0006546f70696341000e31".toByteString //Publish(Header(false,1,false),TopicA,14,ByteVector(1 bytes, 0x31))
      expectMsg("4002000e".toByteString) //Puback(Header(false,0,false),14)

      h ! "820c000f00072f546f7069634102".toByteString //Subscribe(Header(false,1,false),15,Vector((/TopicA,2)))
      expectMsg("9003000f02".toByteString) //Suback(Header(false,0,false),15,Vector(2))

      h ! "320c0007546f7069632f43001031".toByteString //Publish(Header(false,1,false),Topic/C,16,ByteVector(1 bytes, 0x31))
      expectMsg("40020010".toByteString) //Puback(Header(false,0,false),16)

      h ! "340d0008546f706963412f42001131".toByteString //Publish(Header(false,2,false),TopicA/B,17,ByteVector(1 bytes, 0x31))
      expectMsg("50020011".toByteString) //Pubrec(Header(false,0,false),17)

      h ! "62020011".toByteString //Pubrel(Header(false,1,false),17)

      h ! "330e0007546f7069632f430012333333".toByteString //Publish(Header(false,1,true),Topic/C,18,ByteVector(3 bytes, 0x333333))
      expectMsg("70020011".toByteString) //Pubcomp(Header(false,0,false),17)
      expectMsg("40020012".toByteString) //Puback(Header(false,0,false),18)

      h ! "101200044d5154540400003c00066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),normal,None,None,None,None)
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61145" in {
      val h = create_actor("61145")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61146" in {
      val h = create_actor("61146")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61147" in {
      val h = create_actor("61147")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61148" in {
      val h = create_actor("61148")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61149" in {
      val h = create_actor("61149")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61150" in {
      val h = create_actor("61150")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))
      expectMsg("310c0007546f7069632f43333333".toByteString) //Publish(Header(false,0,true),Topic/C,0,ByteVector(3 bytes, 0x333333))
      expectMsg("310b0006546f70696341333333".toByteString) //Publish(Header(false,0,true),TopicA,0,ByteVector(3 bytes, 0x333333))

      h ! "31080006546f70696341".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(empty))

      h ! "31090007546f7069632f43".toByteString //Publish(Header(false,0,true),Topic/C,0,ByteVector(empty))

      expectMsg("30080006546f70696341".toByteString) //Publish(Header(false,0,false),TopicA,0,ByteVector(empty))
      expectMsg("30090007546f7069632f43".toByteString) //Publish(Header(false,0,false),Topic/C,0,ByteVector(empty))
      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61151" in {
      val h = create_actor("61151")

      h ! "100c00044d5154540400003c0000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),,None,None,None,None)
      expectMsg("20020002".toByteString) //Connack(Header(false,0,false),2)

      h ! "330d0008546f706963412f42000131".toByteString //Publish(Header(false,1,true),TopicA/B,1,ByteVector(1 bytes, 0x31))
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61152" in {
      val h = create_actor("61152")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61153" in {
      val h = create_actor("61153")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61154" in {
      val h = create_actor("61154")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61155" in {
      val h = create_actor("61155")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61156" in {
      val h = create_actor("61156")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61157" in {
      val h = create_actor("61157")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61158" in {
      val h = create_actor("61158")

      h ! "101200044d5154540400003c00066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "a206000100022f2b".toByteString //Unsubscribe(Header(false,1,false),1,Vector(/+))
      expectMsg("b0020001".toByteString) //Unsuback(Header(false,0,false),1)

      h ! "320c0008546f706963412f430002".toByteString //Publish(Header(false,1,false),TopicA/C,2,ByteVector(empty))
      expectMsg("40020002".toByteString) //Puback(Header(false,0,false),2)

      h ! "328a100006546f7069634100036c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,1,false),TopicA,3,ByteVector(2048 bytes, #259165444))
      expectMsg("40020003".toByteString) //Puback(Header(false,0,false),3)

      h ! "30891000072f546f706963416c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,false),/TopicA,0,ByteVector(2048 bytes, #259165444))

      h ! "820d00040008546f706963412f2b02".toByteString //Subscribe(Header(false,1,false),4,Vector((TopicA/+,2)))
      expectMsg("9003000402".toByteString) //Suback(Header(false,0,false),4,Vector(2))

      h ! "101200044d5154540402003c00066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,60),normal,None,None,None,None)
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61159" in {
      val h = create_actor("61159")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61160" in {
      val h = create_actor("61160")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61161" in {
      val h = create_actor("61161")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61163" in {
      val h = create_actor("61163")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61164" in {
      val h = create_actor("61164")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61165" in {
      val h = create_actor("61165")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61166" in {
      val h = create_actor("61166")

      h ! "104a00044d5154540402003c003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,60),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8207000100022f2302".toByteString //Subscribe(Header(false,1,false),1,Vector((/#,2)))
      expectMsg("9003000102".toByteString) //Suback(Header(false,0,false),1,Vector(2))

      h ! "a20b00020007546f7069632f43".toByteString //Unsubscribe(Header(false,1,false),2,Vector(Topic/C))
      expectMsg("b0020002".toByteString) //Unsuback(Header(false,0,false),2)

      h ! "330d0008546f706963412f42000331".toByteString //Publish(Header(false,1,true),TopicA/B,3,ByteVector(1 bytes, 0x31))
      expectMsg("40020003".toByteString) //Puback(Header(false,0,false),3)

      h ! "308a100008546f706963412f426c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,false),TopicA/B,0,ByteVector(2048 bytes, #259165444))

      h ! "3189100007546f7069632f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,true),Topic/C,0,ByteVector(2048 bytes, #259165444))

      h ! "300b0006546f70696341333333".toByteString //Publish(Header(false,0,false),TopicA,0,ByteVector(3 bytes, 0x333333))

      h ! "300900072f546f70696341".toByteString //Publish(Header(false,0,false),/TopicA,0,ByteVector(empty))
      expectMsg("300900072f546f70696341".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(empty))

      h ! "820c00040007546f7069632f4300".toByteString //Subscribe(Header(false,1,false),4,Vector((Topic/C,0)))
      expectMsg("9003000400".toByteString) //Suback(Header(false,0,false),4,Vector(0))
      expectMsg("3189100007546f7069632f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,true),Topic/C,0,ByteVector(2048 bytes, #259165444))

      h ! "310900072f546f70696341".toByteString //Publish(Header(false,0,true),/TopicA,0,ByteVector(empty))
      expectMsg("300900072f546f70696341".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(empty))

      h ! "330f0008546f706963412f430005333333".toByteString //Publish(Header(false,1,true),TopicA/C,5,ByteVector(3 bytes, 0x333333))
      expectMsg("40020005".toByteString) //Puback(Header(false,0,false),5)

      h ! "350c00072f546f70696341000631".toByteString //Publish(Header(false,2,true),/TopicA,6,ByteVector(1 bytes, 0x31))
      expectMsg("50020006".toByteString) //Pubrec(Header(false,0,false),6)

      h ! "62020006".toByteString //Pubrel(Header(false,1,false),6)

      h ! "358c100008546f706963412f4300076c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,2,true),TopicA/C,7,ByteVector(2048 bytes, #259165444))
      expectMsg("340c00072f546f70696341000131".toByteString) //Publish(Header(false,2,false),/TopicA,1,ByteVector(1 bytes, 0x31))
      expectMsg("70020006".toByteString) //Pubcomp(Header(false,0,false),6)
      expectMsg("50020007".toByteString) //Pubrec(Header(false,0,false),7)

      h ! "62020007".toByteString //Pubrel(Header(false,1,false),7)

      h ! "50020001".toByteString //Pubrec(Header(false,0,false),1)

      h ! "358c100008546f706963412f4200086c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,2,true),TopicA/B,8,ByteVector(2048 bytes, #259165444))
      expectMsg("70020007".toByteString) //Pubcomp(Header(false,0,false),7)
      expectMsg("62020001".toByteString) //Pubrel(Header(false,1,false),1)
      expectMsg("50020008".toByteString) //Pubrec(Header(false,0,false),8)

      h ! "70020001".toByteString //Pubcomp(Header(false,0,false),1)

      h ! "62020008".toByteString //Pubrel(Header(false,1,false),8)
      expectMsg("70020008".toByteString) //Pubcomp(Header(false,0,false),8)

      h ! "820d00090008546f706963412f2b01".toByteString //Subscribe(Header(false,1,false),9,Vector((TopicA/+,1)))
      expectMsg("9003000901".toByteString) //Suback(Header(false,0,false),9,Vector(1))
      expectMsg("338c100008546f706963412f4200026c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,1,true),TopicA/B,2,ByteVector(2048 bytes, #259165444))
      expectMsg("338c100008546f706963412f4300036c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,1,true),TopicA/C,3,ByteVector(2048 bytes, #259165444))

      h ! "40020003".toByteString //Puback(Header(false,0,false),3)

      h ! "40020002".toByteString //Puback(Header(false,0,false),2)

      h ! "31090006546f7069634131".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(1 bytes, 0x31))

      h ! "340c0008546f706963412f43000a".toByteString //Publish(Header(false,2,false),TopicA/C,10,ByteVector(empty))
      expectMsg("5002000a".toByteString) //Pubrec(Header(false,0,false),10)

      h ! "6202000a".toByteString //Pubrel(Header(false,1,false),10)
      expectMsg("320c0008546f706963412f430004".toByteString) //Publish(Header(false,1,false),TopicA/C,4,ByteVector(empty))
      expectMsg("7002000a".toByteString) //Pubcomp(Header(false,0,false),10)

      h ! "40020004".toByteString //Puback(Header(false,0,false),4)

      h ! "310a0008546f706963412f43".toByteString //Publish(Header(false,0,true),TopicA/C,0,ByteVector(empty))
      expectMsg("300a0008546f706963412f43".toByteString) //Publish(Header(false,0,false),TopicA/C,0,ByteVector(empty))

      h ! "300d0008546f706963412f43333333".toByteString //Publish(Header(false,0,false),TopicA/C,0,ByteVector(3 bytes, 0x333333))
      expectMsg("300d0008546f706963412f43333333".toByteString) //Publish(Header(false,0,false),TopicA/C,0,ByteVector(3 bytes, 0x333333))

      h ! "3089100007546f7069632f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,false),Topic/C,0,ByteVector(2048 bytes, #259165444))
      expectMsg("3089100007546f7069632f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,false),Topic/C,0,ByteVector(2048 bytes, #259165444))

      h ! "8208000b00032b2f4301".toByteString //Subscribe(Header(false,1,false),11,Vector((+/C,1)))
      expectMsg("9003000b01".toByteString) //Suback(Header(false,0,false),11,Vector(1))
      expectMsg("3189100007546f7069632f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,true),Topic/C,0,ByteVector(2048 bytes, #259165444))

      h ! "320d0008546f706963412f42000c31".toByteString //Publish(Header(false,1,false),TopicA/B,12,ByteVector(1 bytes, 0x31))
      expectMsg("4002000c".toByteString) //Puback(Header(false,0,false),12)
      expectMsg("320d0008546f706963412f42000531".toByteString) //Publish(Header(false,1,false),TopicA/B,5,ByteVector(1 bytes, 0x31))

      h ! "40020005".toByteString //Puback(Header(false,0,false),5)

      h ! "350b00072f546f70696341000d".toByteString //Publish(Header(false,2,true),/TopicA,13,ByteVector(empty))
      expectMsg("5002000d".toByteString) //Pubrec(Header(false,0,false),13)

      h ! "6202000d".toByteString //Pubrel(Header(false,1,false),13)

      h ! "340d0008546f706963412f43000e31".toByteString //Publish(Header(false,2,false),TopicA/C,14,ByteVector(1 bytes, 0x31))
      expectMsg("340b00072f546f706963410006".toByteString) //Publish(Header(false,2,false),/TopicA,6,ByteVector(empty))
      expectMsg("7002000d".toByteString) //Pubcomp(Header(false,0,false),13)
      expectMsg("5002000e".toByteString) //Pubrec(Header(false,0,false),14)

      h ! "6202000e".toByteString //Pubrel(Header(false,1,false),14)

      h ! "50020006".toByteString //Pubrec(Header(false,0,false),6)

      h ! "350e0007546f7069632f43000f333333".toByteString //Publish(Header(false,2,true),Topic/C,15,ByteVector(3 bytes, 0x333333))
      expectMsg("320d0008546f706963412f43000731".toByteString) //Publish(Header(false,1,false),TopicA/C,7,ByteVector(1 bytes, 0x31))
      expectMsg("7002000e".toByteString) //Pubcomp(Header(false,0,false),14)
      expectMsg("62020006".toByteString) //Pubrel(Header(false,1,false),6)
      expectMsg("5002000f".toByteString) //Pubrec(Header(false,0,false),15)

      h ! "70020006".toByteString //Pubcomp(Header(false,0,false),6)

      h ! "6202000f".toByteString //Pubrel(Header(false,1,false),15)

      h ! "40020007".toByteString //Puback(Header(false,0,false),7)

      expectMsg("320e0007546f7069632f430008333333".toByteString) //Publish(Header(false,1,false),Topic/C,8,ByteVector(3 bytes, 0x333333))
      expectMsg("7002000f".toByteString) //Pubcomp(Header(false,0,false),15)
      h ! "100c00044d5154540400003c0000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),,None,None,None,None)
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61167" in {
      val h = create_actor("61167")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61168" in {
      val h = create_actor("61168")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61169" in {
      val h = create_actor("61169")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61170" in {
      val h = create_actor("61170")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61171" in {
      val h = create_actor("61171")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61172" in {
      val h = create_actor("61172")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))
      expectMsg("310c0007546f7069632f43333333".toByteString) //Publish(Header(false,0,true),Topic/C,0,ByteVector(3 bytes, 0x333333))
      expectMsg("31090006546f7069634131".toByteString) //Publish(Header(false,0,true),TopicA,0,ByteVector(1 bytes, 0x31))
      expectMsg("318a100008546f706963412f426c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,true),TopicA/B,0,ByteVector(2048 bytes, #259165444))

      h ! "310a0008546f706963412f42".toByteString //Publish(Header(false,0,true),TopicA/B,0,ByteVector(empty))

      h ! "31080006546f70696341".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(empty))

      h ! "31090007546f7069632f43".toByteString //Publish(Header(false,0,true),Topic/C,0,ByteVector(empty))

      expectMsg("300a0008546f706963412f42".toByteString) //Publish(Header(false,0,false),TopicA/B,0,ByteVector(empty))
      expectMsg("30080006546f70696341".toByteString) //Publish(Header(false,0,false),TopicA,0,ByteVector(empty))
      expectMsg("30090007546f7069632f43".toByteString) //Publish(Header(false,0,false),Topic/C,0,ByteVector(empty))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61175" in {
      val h = create_actor("61175")

      h ! "104a00044d5154540402003c003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,60),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "300d0008546f706963412f43333333".toByteString //Publish(Header(false,0,false),TopicA/C,0,ByteVector(3 bytes, 0x333333))

      h ! "338a100006546f7069634100016c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,1,true),TopicA,1,ByteVector(2048 bytes, #259165444))
      expectMsg("40020001".toByteString) //Puback(Header(false,0,false),1)

      h ! "318a100008546f706963412f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,true),TopicA/C,0,ByteVector(2048 bytes, #259165444))

      h ! "358a100006546f7069634100026c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,2,true),TopicA,2,ByteVector(2048 bytes, #259165444))
      expectMsg("50020002".toByteString) //Pubrec(Header(false,0,false),2)

      h ! "62020002".toByteString //Pubrel(Header(false,1,false),2)
      expectMsg("70020002".toByteString) //Pubcomp(Header(false,0,false),2)

      h ! "820d00030008546f706963412f2b00".toByteString //Subscribe(Header(false,1,false),3,Vector((TopicA/+,0)))
      expectMsg("9003000300".toByteString) //Suback(Header(false,0,false),3,Vector(0))
      expectMsg("318a100008546f706963412f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,true),TopicA/C,0,ByteVector(2048 bytes, #259165444))

      h ! "8208000400032b2f2b00".toByteString //Subscribe(Header(false,1,false),4,Vector((+/+,0)))
      expectMsg("9003000400".toByteString) //Suback(Header(false,0,false),4,Vector(0))
      expectMsg("318a100008546f706963412f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,true),TopicA/C,0,ByteVector(2048 bytes, #259165444))

      h ! "330c0008546f706963412f430005".toByteString //Publish(Header(false,1,true),TopicA/C,5,ByteVector(empty))
      expectMsg("40020005".toByteString) //Puback(Header(false,0,false),5)
      expectMsg("300a0008546f706963412f43".toByteString) //Publish(Header(false,0,false),TopicA/C,0,ByteVector(empty))

      h ! "300900072f546f70696341".toByteString //Publish(Header(false,0,false),/TopicA,0,ByteVector(empty))
      expectMsg("300900072f546f70696341".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(empty))

      h ! "320c0008546f706963412f420006".toByteString //Publish(Header(false,1,false),TopicA/B,6,ByteVector(empty))
      expectMsg("40020006".toByteString) //Puback(Header(false,0,false),6)
      expectMsg("300a0008546f706963412f42".toByteString) //Publish(Header(false,0,false),TopicA/B,0,ByteVector(empty))

      h ! "310c00072f546f70696341333333".toByteString //Publish(Header(false,0,true),/TopicA,0,ByteVector(3 bytes, 0x333333))
      expectMsg("300C00072F546F70696341333333".toByteString)

      h ! "350d0008546f706963412f43000731".toByteString //Publish(Header(false,2,true),TopicA/C,7,ByteVector(1 bytes, 0x31))
      expectMsg("50020007".toByteString) //Pubrec(Header(false,0,false),7)

      expectMsg("300b0008546f706963412f4331".toByteString) //Publish(Header(false,0,false),TopicA/C,0,ByteVector(1 bytes, 0x31))

      h ! "62020007".toByteString //Pubrel(Header(false,1,false),7)
      expectMsg("70020007".toByteString) //Pubcomp(Header(false,0,false),7)

      h ! "8207000800022f2301".toByteString //Subscribe(Header(false,1,false),8,Vector((/#,1)))
      expectMsg("9003000801".toByteString) //Suback(Header(false,0,false),8,Vector(1))

      expectMsg("310c00072f546f70696341333333".toByteString) //Publish(Header(false,0,true),/TopicA,0,ByteVector(3 bytes, 0x333333))

      h ! "328a100006546f7069634100096c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,1,false),TopicA,9,ByteVector(2048 bytes, #259165444))
      expectMsg("40020009".toByteString) //Puback(Header(false,0,false),9)

      h ! "8207000a00022f2b01".toByteString //Subscribe(Header(false,1,false),10,Vector((/+,1)))
      expectMsg("9003000a01".toByteString) //Suback(Header(false,0,false),10,Vector(1))
      expectMsg("310c00072f546f70696341333333".toByteString) //Publish(Header(false,0,true),/TopicA,0,ByteVector(3 bytes, 0x333333))

      h ! "3188100006546f706963416c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(2048 bytes, #259165444))

      h ! "8207000b00022f2b01".toByteString //Subscribe(Header(false,1,false),11,Vector((/+,1)))
      expectMsg("9003000b01".toByteString) //Suback(Header(false,0,false),11,Vector(1))
      expectMsg("310c00072f546f70696341333333".toByteString) //Publish(Header(false,0,true),/TopicA,0,ByteVector(3 bytes, 0x333333))

      h ! "340c0008546f706963412f42000c".toByteString //Publish(Header(false,2,false),TopicA/B,12,ByteVector(empty))
      expectMsg("5002000c".toByteString) //Pubrec(Header(false,0,false),12)

      h ! "6202000c".toByteString //Pubrel(Header(false,1,false),12)

      h ! "820c000d0007546f7069632f4302".toByteString //Subscribe(Header(false,1,false),13,Vector((Topic/C,2)))
      expectMsg("300a0008546f706963412f42".toByteString) //Publish(Header(false,0,false),TopicA/B,0,ByteVector(empty))
      expectMsg("7002000c".toByteString) //Pubcomp(Header(false,0,false),12)
      expectMsg("9003000d02".toByteString) //Suback(Header(false,0,false),13,Vector(2))

      h ! "820d000e0008546f706963412f2b02".toByteString //Subscribe(Header(false,1,false),14,Vector((TopicA/+,2)))
      expectMsg("9003000e02".toByteString) //Suback(Header(false,0,false),14,Vector(2))
      expectMsg("350d0008546f706963412f43000131".toByteString) //Publish(Header(false,2,true),TopicA/C,1,ByteVector(1 bytes, 0x31))

      h ! "50020001".toByteString //Pubrec(Header(false,0,false),1)

      h ! "330f0008546f706963412f43000f333333".toByteString //Publish(Header(false,1,true),TopicA/C,15,ByteVector(3 bytes, 0x333333))
      expectMsg("62020001".toByteString) //Pubrel(Header(false,1,false),1)
      expectMsg("4002000f".toByteString) //Puback(Header(false,0,false),15)
      expectMsg("320f0008546f706963412f430002333333".toByteString) //Publish(Header(false,1,false),TopicA/C,2,ByteVector(3 bytes, 0x333333))

      h ! "70020001".toByteString //Pubcomp(Header(false,0,false),1)

      h ! "40020002".toByteString //Puback(Header(false,0,false),2)

      h ! "320a0006546f706963410010".toByteString //Publish(Header(false,1,false),TopicA,16,ByteVector(empty))
      expectMsg("40020010".toByteString) //Puback(Header(false,0,false),16)

      h ! "3088100006546f706963416c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,false),TopicA,0,ByteVector(2048 bytes, #259165444))

      h ! "320c0007546f7069632f43001131".toByteString //Publish(Header(false,1,false),Topic/C,17,ByteVector(1 bytes, 0x31))
      expectMsg("40020011".toByteString) //Puback(Header(false,0,false),17)
      expectMsg("320c0007546f7069632f43000331".toByteString) //Publish(Header(false,1,false),Topic/C,3,ByteVector(1 bytes, 0x31))

      h ! "40020003".toByteString //Puback(Header(false,0,false),3)

      h ! "310a0008546f706963412f42".toByteString //Publish(Header(false,0,true),TopicA/B,0,ByteVector(empty))
      expectMsg("300a0008546f706963412f42".toByteString) //Publish(Header(false,0,false),TopicA/B,0,ByteVector(empty))

      h ! "358c100008546f706963412f4300126c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,2,true),TopicA/C,18,ByteVector(2048 bytes, #259165444))
      expectMsg("50020012".toByteString) //Pubrec(Header(false,0,false),18)

      h ! "62020012".toByteString //Pubrel(Header(false,1,false),18)

      h ! "310a00072f546f7069634131".toByteString //Publish(Header(false,0,true),/TopicA,0,ByteVector(1 bytes, 0x31))
      expectMsg("348c100008546f706963412f4300046c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,2,false),TopicA/C,4,ByteVector(2048 bytes, #259165444))
      expectMsg("70020012".toByteString) //Pubcomp(Header(false,0,false),18)
      expectMsg("300a00072f546f7069634131".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(1 bytes, 0x31))

      h ! "50020004".toByteString //Pubrec(Header(false,0,false),4)

      h ! "8207001300022f2b02".toByteString //Subscribe(Header(false,1,false),19,Vector((/+,2)))
      expectMsg("62020004".toByteString) //Pubrel(Header(false,1,false),4)
      expectMsg("9003001302".toByteString) //Suback(Header(false,0,false),19,Vector(2))
      expectMsg("310a00072f546f7069634131".toByteString) //Publish(Header(false,0,true),/TopicA,0,ByteVector(1 bytes, 0x31))

      h ! "70020004".toByteString //Pubcomp(Header(false,0,false),4)

      h ! "340d0008546f706963412f43001431".toByteString //Publish(Header(false,2,false),TopicA/C,20,ByteVector(1 bytes, 0x31))
      expectMsg("50020014".toByteString) //Pubrec(Header(false,0,false),20)

      h ! "62020014".toByteString //Pubrel(Header(false,1,false),20)

      h ! "a20a00150006546f70696341".toByteString //Unsubscribe(Header(false,1,false),21,Vector(TopicA))
      expectMsg("340d0008546f706963412f43000531".toByteString) //Publish(Header(false,2,false),TopicA/C,5,ByteVector(1 bytes, 0x31))
      expectMsg("70020014".toByteString) //Pubcomp(Header(false,0,false),20)
      expectMsg("b0020015".toByteString) //Unsuback(Header(false,0,false),21)

      h ! "50020005".toByteString //Pubrec(Header(false,0,false),5)

      h ! "340a0006546f706963410016".toByteString //Publish(Header(false,2,false),TopicA,22,ByteVector(empty))
      expectMsg("62020005".toByteString) //Pubrel(Header(false,1,false),5)
      expectMsg("50020016".toByteString) //Pubrec(Header(false,0,false),22)

      h ! "70020005".toByteString //Pubcomp(Header(false,0,false),5)

      h ! "62020016".toByteString //Pubrel(Header(false,1,false),22)

      h ! "a20b001700072f546f70696341".toByteString //Unsubscribe(Header(false,1,false),23,Vector(/TopicA))
      expectMsg("70020016".toByteString) //Pubcomp(Header(false,0,false),22)
      expectMsg("b0020017".toByteString) //Unsuback(Header(false,0,false),23)

      h ! "104a00044d5154540400003c003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61177" in {
      val h = create_actor("61177")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61178" in {
      val h = create_actor("61178")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61179" in {
      val h = create_actor("61179")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61180" in {
      val h = create_actor("61180")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61181" in {
      val h = create_actor("61181")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61182" in {
      val h = create_actor("61182")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))
      expectMsg("310a00072f546f7069634131".toByteString) //Publish(Header(false,0,true),/TopicA,0,ByteVector(1 bytes, 0x31))
      expectMsg("3188100006546f706963416c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,true),TopicA,0,ByteVector(2048 bytes, #259165444))
      expectMsg("318a100008546f706963412f436c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,true),TopicA/C,0,ByteVector(2048 bytes, #259165444))

      h ! "31080006546f70696341".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(empty))

      h ! "310900072f546f70696341".toByteString //Publish(Header(false,0,true),/TopicA,0,ByteVector(empty))

      h ! "310a0008546f706963412f43".toByteString //Publish(Header(false,0,true),TopicA/C,0,ByteVector(empty))

      expectMsg("30080006546f70696341".toByteString) //Publish(Header(false,0,false),TopicA,0,ByteVector(empty))
      expectMsg("300900072f546f70696341".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(empty))
      expectMsg("300a0008546f706963412f43".toByteString) //Publish(Header(false,0,false),TopicA/C,0,ByteVector(empty))
      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61183" in {
      val h = create_actor("61183")

      h ! "100c00044d5154540402003c0000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,60),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "340c00072f546f70696341000131".toByteString //Publish(Header(false,2,false),/TopicA,1,ByteVector(1 bytes, 0x31))
      expectMsg("50020001".toByteString) //Pubrec(Header(false,0,false),1)

      h ! "62020001".toByteString //Pubrel(Header(false,1,false),1)

      h ! "8207000200022f2b02".toByteString //Subscribe(Header(false,1,false),2,Vector((/+,2)))
      expectMsg("70020001".toByteString) //Pubcomp(Header(false,0,false),1)
      expectMsg("9003000202".toByteString) //Suback(Header(false,0,false),2,Vector(2))

      h ! "320c0008546f706963412f420003".toByteString //Publish(Header(false,1,false),TopicA/B,3,ByteVector(empty))
      expectMsg("40020003".toByteString) //Puback(Header(false,0,false),3)

      h ! "310b0006546f70696341333333".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(3 bytes, 0x333333))

      h ! "300b0008546f706963412f4331".toByteString //Publish(Header(false,0,false),TopicA/C,0,ByteVector(1 bytes, 0x31))

      h ! "350f0008546f706963412f430004333333".toByteString //Publish(Header(false,2,true),TopicA/C,4,ByteVector(3 bytes, 0x333333))
      expectMsg("50020004".toByteString) //Pubrec(Header(false,0,false),4)

      h ! "62020004".toByteString //Pubrel(Header(false,1,false),4)

      h ! "30090007546f7069632f43".toByteString //Publish(Header(false,0,false),Topic/C,0,ByteVector(empty))
      expectMsg("70020004".toByteString) //Pubcomp(Header(false,0,false),4)

      h ! "820c00050007546f7069632f4301".toByteString //Subscribe(Header(false,1,false),5,Vector((Topic/C,1)))
      expectMsg("9003000501".toByteString) //Suback(Header(false,0,false),5,Vector(1))

      h ! "330c0008546f706963412f430006".toByteString //Publish(Header(false,1,true),TopicA/C,6,ByteVector(empty))
      expectMsg("40020006".toByteString) //Puback(Header(false,0,false),6)

      h ! "30080006546f70696341".toByteString //Publish(Header(false,0,false),TopicA,0,ByteVector(empty))

      h ! "820c000700072f546f7069634101".toByteString //Subscribe(Header(false,1,false),7,Vector((/TopicA,1)))
      expectMsg("9003000701".toByteString) //Suback(Header(false,0,false),7,Vector(1))

      h ! "320e00072f546f706963410008333333".toByteString //Publish(Header(false,1,false),/TopicA,8,ByteVector(3 bytes, 0x333333))
      expectMsg("40020008".toByteString) //Puback(Header(false,0,false),8)
      expectMsg("320e00072f546f706963410001333333".toByteString) //Publish(Header(false,1,false),/TopicA,1,ByteVector(3 bytes, 0x333333))

      h ! "40020001".toByteString //Puback(Header(false,0,false),1)

      h ! "8207000900022f2b01".toByteString //Subscribe(Header(false,1,false),9,Vector((/+,1)))
      expectMsg("9003000901".toByteString) //Suback(Header(false,0,false),9,Vector(1))

      h ! "330c00072f546f70696341000a31".toByteString //Publish(Header(false,1,true),/TopicA,10,ByteVector(1 bytes, 0x31))
      expectMsg("4002000a".toByteString) //Puback(Header(false,0,false),10)
      expectMsg("320c00072f546f70696341000231".toByteString) //Publish(Header(false,1,false),/TopicA,2,ByteVector(1 bytes, 0x31))

      h ! "40020002".toByteString //Puback(Header(false,0,false),2)

      h ! "104a00044d5154540400003c003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61184" in {
      val h = create_actor("61184")

      h ! "100c00044d515454040200000000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61185" in {
      val h = create_actor("61185")

      h ! "101200044d5154540402000000066e6f726d616c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),normal,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61186" in {
      val h = create_actor("61186")

      h ! "102300044d5154540402000000173233206368617261637465727334353637383930313233".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),23 characters4567890123,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61187" in {
      val h = create_actor("61187")

      h ! "103500044d5154540402000000294120636c69656e746964207468617420697320746f6f206c6f6e67202d2073686f756c64206661696c".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is too long - should fail,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61188" in {
      val h = create_actor("61188")

      h ! "104a00044d51545404020000003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61189" in {
      val h = create_actor("61189")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))
      expectMsg("310a00072f546f7069634131".toByteString) //Publish(Header(false,0,true),/TopicA,0,ByteVector(1 bytes, 0x31))
      expectMsg("310b0006546f70696341333333".toByteString) //Publish(Header(false,0,true),TopicA,0,ByteVector(3 bytes, 0x333333))

      h ! "31080006546f70696341".toByteString //Publish(Header(false,0,true),TopicA,0,ByteVector(empty))

      h ! "310900072f546f70696341".toByteString //Publish(Header(false,0,true),/TopicA,0,ByteVector(empty))

      expectMsg("30080006546f70696341".toByteString) //Publish(Header(false,0,false),TopicA,0,ByteVector(empty))
      expectMsg("300900072f546f70696341".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(empty))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 61190" in {
      val h = create_actor("61190")

      h ! "100c00044d5154540402003c0000".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,60),,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "350d0006546f706963410001333333".toByteString //Publish(Header(false,2,true),TopicA,1,ByteVector(3 bytes, 0x333333))
      expectMsg("50020001".toByteString) //Pubrec(Header(false,0,false),1)

      h ! "62020001".toByteString //Pubrel(Header(false,1,false),1)

      h ! "820c000200072f546f7069634102".toByteString //Subscribe(Header(false,1,false),2,Vector((/TopicA,2)))
      expectMsg("70020001".toByteString) //Pubcomp(Header(false,0,false),1)
      expectMsg("9003000202".toByteString) //Suback(Header(false,0,false),2,Vector(2))

      h ! "358c100008546f706963412f4300036c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,2,true),TopicA/C,3,ByteVector(2048 bytes, #259165444))
      expectMsg("50020003".toByteString) //Pubrec(Header(false,0,false),3)

      h ! "62020003".toByteString //Pubrel(Header(false,1,false),3)

      h ! "330e0007546f7069632f430004333333".toByteString //Publish(Header(false,1,true),Topic/C,4,ByteVector(3 bytes, 0x333333))
      expectMsg("70020003".toByteString) //Pubcomp(Header(false,0,false),3)
      expectMsg("40020004".toByteString) //Puback(Header(false,0,false),4)

      h ! "8207000500022f2b01".toByteString //Subscribe(Header(false,1,false),5,Vector((/+,1)))
      expectMsg("9003000501".toByteString) //Suback(Header(false,0,false),5,Vector(1))

      h ! "320d0008546f706963412f42000631".toByteString //Publish(Header(false,1,false),TopicA/B,6,ByteVector(1 bytes, 0x31))
      expectMsg("40020006".toByteString) //Puback(Header(false,0,false),6)

      h ! "30891000072f546f706963416c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,0,false),/TopicA,0,ByteVector(2048 bytes, #259165444))
      expectMsg("30891000072f546f706963416c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,0,false),/TopicA,0,ByteVector(2048 bytes, #259165444))

      h ! "a20c00070008546f706963412f42".toByteString //Unsubscribe(Header(false,1,false),7,Vector(TopicA/B))
      expectMsg("b0020007".toByteString) //Unsuback(Header(false,0,false),7)

      h ! "350b00072f546f706963410008".toByteString //Publish(Header(false,2,true),/TopicA,8,ByteVector(empty))
      expectMsg("50020008".toByteString) //Pubrec(Header(false,0,false),8)

      h ! "62020008".toByteString //Pubrel(Header(false,1,false),8)

      h ! "300a0008546f706963412f42".toByteString //Publish(Header(false,0,false),TopicA/B,0,ByteVector(empty))
      expectMsg("340b00072f546f706963410001".toByteString) //Publish(Header(false,2,false),/TopicA,1,ByteVector(empty))
      expectMsg("70020008".toByteString) //Pubcomp(Header(false,0,false),8)

      h ! "50020001".toByteString //Pubrec(Header(false,0,false),1)

      h ! "350f0008546f706963412f430009333333".toByteString //Publish(Header(false,2,true),TopicA/C,9,ByteVector(3 bytes, 0x333333))
      expectMsg("62020001".toByteString) //Pubrel(Header(false,1,false),1)
      expectMsg("50020009".toByteString) //Pubrec(Header(false,0,false),9)

      h ! "70020001".toByteString //Pubcomp(Header(false,0,false),1)

      h ! "62020009".toByteString //Pubrel(Header(false,1,false),9)

      h ! "340a0006546f70696341000a".toByteString //Publish(Header(false,2,false),TopicA,10,ByteVector(empty))
      expectMsg("70020009".toByteString) //Pubcomp(Header(false,0,false),9)
      expectMsg("5002000a".toByteString) //Pubrec(Header(false,0,false),10)

      h ! "6202000a".toByteString //Pubrel(Header(false,1,false),10)

      h ! "8208000b00032b2f2b02".toByteString //Subscribe(Header(false,1,false),11,Vector((+/+,2)))
      expectMsg("7002000a".toByteString) //Pubcomp(Header(false,0,false),10)
      expectMsg("9003000b02".toByteString) //Suback(Header(false,0,false),11,Vector(2))
      expectMsg("330e0007546f7069632f430002333333".toByteString) //Publish(Header(false,1,true),Topic/C,2,ByteVector(3 bytes, 0x333333))
      expectMsg("350f0008546f706963412f430003333333".toByteString) //Publish(Header(false,2,true),TopicA/C,3,ByteVector(3 bytes, 0x333333))

      h ! "50020003".toByteString //Pubrec(Header(false,0,false),3)
      expectMsg("62020003".toByteString) //Pubrel(Header(false,1,false),3)

      h ! "40020002".toByteString //Puback(Header(false,0,false),2)

      h ! "350c0008546f706963412f42000c".toByteString //Publish(Header(false,2,true),TopicA/B,12,ByteVector(empty))
      expectMsg("5002000c".toByteString) //Pubrec(Header(false,0,false),12)

      h ! "70020003".toByteString //Pubcomp(Header(false,0,false),3)

      h ! "6202000c".toByteString //Pubrel(Header(false,1,false),12)

      h ! "330c0007546f7069632f43000d31".toByteString //Publish(Header(false,1,true),Topic/C,13,ByteVector(1 bytes, 0x31))
      expectMsg("340c0008546f706963412f420004".toByteString) //Publish(Header(false,2,false),TopicA/B,4,ByteVector(empty))
      expectMsg("7002000c".toByteString) //Pubcomp(Header(false,0,false),12)
      expectMsg("4002000d".toByteString) //Puback(Header(false,0,false),13)
      expectMsg("320c0007546f7069632f43000531".toByteString) //Publish(Header(false,1,false),Topic/C,5,ByteVector(1 bytes, 0x31))

      h ! "40020005".toByteString //Puback(Header(false,0,false),5)

      h ! "50020004".toByteString //Pubrec(Header(false,0,false),4)

      h ! "340a0006546f70696341000e".toByteString //Publish(Header(false,2,false),TopicA,14,ByteVector(empty))
      expectMsg("62020004".toByteString) //Pubrel(Header(false,1,false),4)
      expectMsg("5002000e".toByteString) //Pubrec(Header(false,0,false),14)

      h ! "70020004".toByteString //Pubcomp(Header(false,0,false),4)

      h ! "6202000e".toByteString //Pubrel(Header(false,1,false),14)

      h ! "348c100008546f706963412f43000f6c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString //Publish(Header(false,2,false),TopicA/C,15,ByteVector(2048 bytes, #259165444))
      expectMsg("7002000e".toByteString) //Pubcomp(Header(false,0,false),14)
      expectMsg("5002000f".toByteString) //Pubrec(Header(false,0,false),15)

      h ! "6202000f".toByteString //Pubrel(Header(false,1,false),15)

      h ! "8207001000022f2b01".toByteString //Subscribe(Header(false,1,false),16,Vector((/+,1)))
      expectMsg("348c100008546f706963412f4300066c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e676c6f6e67".toByteString) //Publish(Header(false,2,false),TopicA/C,6,ByteVector(2048 bytes, #259165444))
      expectMsg("7002000f".toByteString) //Pubcomp(Header(false,0,false),15)
      expectMsg("9003001001".toByteString) //Suback(Header(false,0,false),16,Vector(1))

      h ! "50020006".toByteString //Pubrec(Header(false,0,false),6)

      h ! "a206001100022f2b".toByteString //Unsubscribe(Header(false,1,false),17,Vector(/+))
      expectMsg("62020006".toByteString) //Pubrel(Header(false,1,false),6)
      expectMsg("b0020011".toByteString) //Unsuback(Header(false,0,false),17)

      h ! "70020006".toByteString //Pubcomp(Header(false,0,false),6)

      h ! "104a00044d5154540400003c003e4120636c69656e7469642074686174206973206c6f6e676572207468616e203233206368617273202d2073686f756c6420776f726b20696e20332e312e31".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,60),A clientid that is longer than 23 chars - should work in 3.1.1,None,None,None,None)
      expectMsgType[Failure]
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}

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

class WillSpec extends TestKit(ActorSystem("WillSpec")) with ImplicitSender with SpecificationLike with Scope {

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

    "Scenario 53180" in {
      val h = create_actor("53180")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53181" in {
      val h = create_actor("53181")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53182" in {
      val h = create_actor("53182")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toByteString //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toByteString) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 53183 + 53184" in {
      val h3 = create_actor("53183")

      h3 ! "103800044d51545404160002000a6d79636c69656e7469640007546f7069632f430017636c69656e74206e6f7420646973636f6e6e6563746564".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,2,true,true,2),myclientid,Some(Topic/C),Some(client not disconnected),None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      val h4 = create_actor("53184")

      h4 ! "101700044d51545404000000000b6d79636c69656e74696432".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,false,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h4 ! "820c00020007546f7069632f4302".toByteString //Subscribe(Header(false,1,false),2,Vector((Topic/C,2)))
      expectMsg("9003000202".toByteString) //Suback(Header(false,0,false),2,Vector(2))

      h3 ! Tcp.PeerClosed
      expectMsg(Tcp.Close)

      expectMsg("34220007546f7069632f430001636c69656e74206e6f7420646973636f6e6e6563746564".toByteString) //Publish(Header(false,2,false),Topic/C,1,ByteVector(23 bytes, 0x636c69656e74206e6f7420646973636f6e6e6563746564))

      h4 ! "50020001".toByteString //Pubrec(Header(false,0,false),1)
      expectMsg("62020001".toByteString) //Pubrel(Header(false,1,false),1)

      h4 ! "70020001".toByteString //Pubcomp(Header(false,0,false),1)

      h4 ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}

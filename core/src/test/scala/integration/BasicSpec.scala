package integration

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.broker._
import Helpers._
import net.jetmq.tests.Bag
import org.specs2.mutable._
import org.specs2.specification.Scope

class BasicSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    def create_actor(name: String) = {
      system.actorOf(Props(new TcpConnectionActor(devices)).withMailbox("priority-dispatcher"), name)
    }

    "Scenario 51208" in {
      val h = create_actor("51208")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51209" in {
      val h = create_actor("51209")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51210" in {
      val h = create_actor("51210")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toTcpReceived //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toTcpWrite) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51211" in {
      val h = create_actor("51211")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51212" in {
      val h = create_actor("51212")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "820b00020006546f7069634102".toTcpReceived //Subscribe(Header(false,1,false),2,Vector((TopicA,2)))
      expectMsg("9003000202".toTcpWrite) //Suback(Header(false,0,false),2,Vector(2))

      h ! "300d0006546f70696341716f732030320f0006546f706963410003716f732031340f0006546f706963410004716f732032".toTcpReceived //Publish(Header(false,0,false),TopicA,0,ByteVector(5 bytes, 0x716f732030))
      expectMsg("40020003".toTcpWrite) //Puback(Header(false,0,false),3)
      expectMsg("50020004".toTcpWrite) //Pubrec
      expectMsg("300d0006546f70696341716f732030".toTcpWrite) //Publish(Header(false,0,false),TopicA,0,ByteVector(5 bytes, 0x716f732030))
      expectMsg("320f0006546f706963410001716f732031".toTcpWrite) //Publish(Header(false,1,false),TopicA,1,ByteVector(5 bytes, 0x716f732031))

      h ! "40020001".toTcpReceived //Puback(Header(false,0,false),1)

      h ! "62020004".toTcpReceived //Pubrel(Header(false,1,false),4)
      expectMsg("340f0006546f706963410002716f732032".toTcpWrite) //Publish(Header(false,2,false),TopicA,2,ByteVector(5 bytes, 0x716f732032))
      expectMsg("70020004".toTcpWrite) //Pubcomp(Header(false,0,false),4)

      h ! "50020002".toTcpReceived //Pubrec(Header(false,0,false),2)
      expectMsg("62020002".toTcpWrite) //Pubrel(Header(false,1,false),2)

      h ! "70020002".toTcpReceived //Pubcomp(Header(false,0,false),2)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51213" in {
      val h = create_actor("51213")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 51214" in {
      val h = create_actor("51214")

      h ! "10140002686a04020000000a6d79636c69656e746964".toTcpReceived //Broken package
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}

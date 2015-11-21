package integration

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.Helpers._
import net.jetmq.SessionsManagerActor
import net.jetmq.broker.{Bag, EventBusActor, TcpConnectionActor}
import org.specs2.mutable._
import org.specs2.specification.Scope

class KeepaliveSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {
  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    def create_actor(name: String) = {
      system.actorOf(Props(new TcpConnectionActor(devices)).withMailbox("priority-dispatcher"), name)
    }


    "Scenario 59073" in {
      val h = create_actor("59073")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 59075" in {
      val h = create_actor("59075")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 59076" in {
      val h = create_actor("59076")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toTcpReceived //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toTcpWrite) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 59080 + 59081" in {

      val h1 = create_actor("59080")

      h1 ! "103100044d51545404160005000a6d79636c69656e74696400072f546f7069634100106b656570616c69766520657870697279".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,2,true,true,5),myclientid,Some(/TopicA),Some(keepalive expiry),None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      val h2 = create_actor("59081")

      h2 ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h2 ! "820c000200072f546f7069634102".toTcpReceived //Subscribe(Header(false,1,false),2,Vector((/TopicA,2)))
      expectMsg("9003000202".toTcpWrite) //Suback(Header(false,0,false),2,Vector(2))

      expectMsg(Bag.ten_sec, Tcp.Close)
      expectMsg(Bag.ten_sec, "341b00072f546f7069634100016b656570616c69766520657870697279".toTcpWrite) //Publish(Header(false,2,false),/TopicA,1,ByteVector(16 bytes, 0x6b656570616c69766520657870697279))

      h2 ! "50020001".toTcpReceived //Pubrec(Header(false,0,false),1)
      expectMsg("62020001".toTcpWrite) //Pubrel(Header(false,1,false),1)

      h2 ! "70020001".toTcpReceived //Pubcomp(Header(false,0,false),1)

      h2 ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}

package integration

import akka.actor.{ActorSystem, Props}
import akka.io.Tcp
import akka.testkit.{ImplicitSender, TestKit}
import net.jetmq.Helpers._
import net.jetmq.SessionsManagerActor
import net.jetmq.broker.{Bag, EventBusActor, TcpConnectionActor}
import org.specs2.mutable._
import org.specs2.specification.Scope

class OverlappingSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with Scope {

  sequential //state dependant

  "Requests handler actor" should {

    val bus = system.actorOf(Props[EventBusActor], "bus")
    val devices = system.actorOf(Props(new SessionsManagerActor(bus)), "devices")

    def create_actor(name: String) = {
      system.actorOf(Props(new TcpConnectionActor(devices)).withMailbox("priority-dispatcher"), name)
    }

    "Scenario 58618" in {
      val h = create_actor("58618")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 58619" in {
      val h = create_actor("58619")

      h ! "101700044d51545404020000000b6d79636c69656e74696432".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid2,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 58620" in {
      val h = create_actor("58620")

      h ! "101a00044d51545404020000000e636c65616e2072657461696e6564".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),clean retained,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "8206000200012300".toTcpReceived //Subscribe(Header(false,1,false),2,Vector((#,0)))
      expectMsg("9003000200".toTcpWrite) //Suback(Header(false,0,false),2,Vector(0))

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

    "Scenario 58621" in {
      val h = create_actor("58621")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toTcpReceived //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toTcpWrite) //Connack(Header(false,0,false),0)

      h ! "821800020008546f706963412f23020008546f706963412f2b01".toTcpReceived //Subscribe(Header(false,1,false),2,Vector((TopicA/#,2), (TopicA/+,1)))

      h ! "34250008546f706963412f4300036f7665726c617070696e6720746f7069632066696c74657273".toTcpReceived //Publish(Header(false,2,false),TopicA/C,3,ByteVector(25 bytes, 0x6f7665726c617070696e6720746f7069632066696c74657273))
      expectMsg("900400020201".toTcpWrite) //Suback(Header(false,0,false),2,Vector(2, 1))
      expectMsg("50020003".toTcpWrite) //Pubrec(Header(false,0,false),3)

      h ! "62020003".toTcpReceived //Pubrel(Header(false,1,false),3)
      expectMsg("70020003".toTcpWrite) //Pubcomp(Header(false,0,false),3)

      expectMsg("34250008546f706963412f4300016f7665726c617070696e6720746f7069632066696c74657273".toTcpWrite) //Publish(Header(false,2,false),TopicA/C,1,ByteVector(25 bytes, 0x6f7665726c617070696e6720746f7069632066696c74657273))

      h ! "50020001".toTcpReceived //Pubrec(Header(false,0,false),1)
      expectMsg("62020001".toTcpWrite) //Pubrel(Header(false,1,false),1)

      h ! "70020001".toTcpReceived //Pubcomp(Header(false,0,false),1)

      h ! "e000".toTcpReceived //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }

  }
}

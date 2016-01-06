package integration

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.Tcp
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import net.jetmq.broker.Helpers._
import net.jetmq.broker._
import net.jetmq.tests.Bag
import org.specs2.mutable._
import org.specs2.specification.Scope

class StreamSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender with SpecificationLike with Scope {

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

    "Scenario 51208" in {
      val h = create_actor("51208")

      h ! "101600044d51545404020000000a6d79636c69656e746964".toByteString //Connect(Header(false,0,false),ConnectFlags(false,false,false,0,false,true,0),myclientid,None,None,None,None)
      expectMsg("20020000".toByteString) //Connack(Header(false,0,false),0)

      h ! "e000".toByteString //Disconnect(Header(false,0,false))
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}

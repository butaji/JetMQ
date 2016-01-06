package ws

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.Tcp
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import integration.TcpConnectionActor
import net.jetmq.broker.Helpers._
import net.jetmq.broker._
import net.jetmq.tests.Bag
import org.specs2.mutable._
import org.specs2.specification.Scope

class WsSpec extends TestKit(ActorSystem("WsSpec")) with ImplicitSender with SpecificationLike with Scope {

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

    "Connect to Ws" in {

      val h = create_actor("53180")

      h ! "102100064d51497364700302003c0013636c69656e7449642d304a584b454b6667547a".toByteString

      expectMsg("20020000".toByteString)

      h ! "e000".toByteString
      expectMsg(Tcp.Close)
      expectNoMsg(Bag.wait_time)
      success
    }
  }
}
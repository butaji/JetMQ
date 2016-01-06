package integration

import akka.actor.Status.Failure
import akka.actor.{ActorRef, PoisonPill, Props}
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import net.jetmq.broker.Helpers._
import net.jetmq.broker._

class TcpConnectionActor(devices:ActorRef) extends ActorPublisherWithBuffer[ByteString] {

  val mqtt = context.actorOf(Props(new MqttConnectionActor(devices)))
  var pub = ActorPublisher[Packet](mqtt)

  implicit val sys = context.system
  implicit val mat = ActorMaterializer()

  Source(pub).to(Sink.actorRef(self, PoisonPill)).run()

  def receive = {

    case s:ByteString => PacketsHelper
      .decode(s.toBitVector)
      .foreach(mqtt ! OnNext(_))

    case r:Request => {
      deliverBuffer()
    }

    case p:Packet => {
      onNextBuffered(ByteString(PacketsHelper.encode(p).require.toByteArray))
    }

    case Failure(err:Throwable) => onErrorThenStop(err)

    case x => {
      println("AAAAAAA " + x + " " + x.getClass().getCanonicalName())
    }
  }

}

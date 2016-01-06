package net.jetmq.broker

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.actor.{ActorPublisher, ActorSubscriber}
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString
import net.jetmq.broker.Helpers._

object BrokerServer {

  def run(system: ActorSystem) = {

    val bus = system.actorOf(Props[EventBusActor], name = "event-bus")
    val sessions = system.actorOf(Props(new SessionsManagerActor(bus)), name = "sessions")

    implicit val materializer = ActorMaterializer()(system)
    import system.dispatcher

    Tcp(system).bind(interface = "0.0.0.0", port = 1883)
      .to(Sink.foreach { connection =>

        println("Accepted new connection from " + connection.remoteAddress)

        val connectionActor = system.actorOf(Props(new MqttConnectionActor(sessions)))

        def logger[T](prefix: String) = Flow[T].map(msg => {
          println(s"$prefix > ${msg}")
          msg
        })

        val publisher = ActorPublisher[Packet](connectionActor)

        val source = Source(publisher)
          .via(logger("mqtt < "))
          .map(PacketsHelper.encode(_).require)
          .map(_.toByteArray)
          .map(ByteString(_))

        val subscriber = ActorSubscriber[Packet](connectionActor)

        val sink = Flow[ByteString]
          .via(logger("row > "))
          .map(x => x.toBitVector)
          .mapConcat(PacketsHelper.decode(_))
          .via(logger("mqtt > "))
          .to(Sink(subscriber))

        connection handleWith Flow.fromSinkAndSource(sink, source)
      })
      .run().onComplete(println)
  }
}

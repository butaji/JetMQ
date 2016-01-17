package net.jetmq.broker

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, UpgradeToWebsocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.routing.{BroadcastGroup, RoundRobinGroup}
import akka.stream.ActorMaterializer
import akka.stream.actor.{ActorPublisher, ActorSubscriber}
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString
import net.jetmq.broker.Helpers._

object BrokerServer {

  def run(system: ActorSystem) = {

    implicit val context = system.dispatcher


    val paths = (1 to 1000).map(x => {
      system.actorOf(Props[EventBusActor], name = "event-bus-" + x).path.toString
    })

    val bus_publisher = system.actorOf(RoundRobinGroup(paths).props())
    val bus_manager = system.actorOf(BroadcastGroup(paths).props())

    val sessions = system.actorOf(Props(new SessionsManagerActor(bus_manager, bus_publisher)), name = "sessions")

    implicit val materializer = ActorMaterializer()(system)

    Tcp(system).bind(interface = "0.0.0.0", port = 1883).runForeach { connection =>

      println("Accepted new connection from " + connection.remoteAddress)

      val (sink, source) = ConstructSinkAndSource(system, sessions, connection.remoteAddress)

      connection handleWith Flow.fromSinkAndSource(sink, source)
    }
      .onComplete(x => {
        println(x)
      })


    def requestHadler(connection: IncomingConnection, r: HttpRequest): HttpResponse = {
      r match {
        case req@HttpRequest(GET, Uri.Path("/mqtt"), _, _, _) =>
          req.header[UpgradeToWebsocket] match {
            case Some(upgrade) => {


              val (sinkByteString, sourceByteString) = ConstructSinkAndSource(system, sessions, connection.remoteAddress)

              val sink = Flow[Message].map {
                case BinaryMessage.Strict(d) => d
                case _ => ByteString()
              }.to(sinkByteString)

              val source = sourceByteString.map(BinaryMessage(_))

              upgrade.handleMessagesWithSinkSource(sink, source)
            }
            case None => HttpResponse(400, entity = "Not a valid websocket request!")
          }
        case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")

      }
    }

    println("Http binding...")
    Http(system).bind(interface = "0.0.0.0", port = 8080).runForeach { connection =>
      println("Accepted new connection from " + connection.remoteAddress)

      val handler: (HttpRequest) => HttpResponse = r => requestHadler(connection, r)
      connection.handleWithSyncHandler(handler)
    }
      .onComplete(x => {
        println(x)
      })

  }

  def logger[T](prefix: String) = Flow[T].map(msg => {

    println(s"$prefix $msg")
    msg
  })

  def ConstructSinkAndSource(system: ActorSystem, sessions: ActorRef, remoteAddress: InetSocketAddress): (Sink[ByteString, Unit], Source[ByteString, Unit]) = {

    val name = remoteAddress.getHostString() + ":" + remoteAddress.getPort()

    val connectionActor = system.actorOf(Props(new MqttConnectionActor(sessions)), "connection-" + name)

    val publisher = ActorPublisher[Packet](connectionActor)

    val source = Source(publisher)
      .via(logger("mqtt < "))
      .map(PacketsHelper.encode(_).require)
      .map(_.toByteArray)
      .map(ByteString(_))

    val subscriber = ActorSubscriber[Packet](connectionActor)

    val sink = Flow[ByteString]
      .map(x => x.toBitVector)
      .mapConcat(PacketsHelper.decode)
      .via(logger("mqtt > "))
      .to(Sink(subscriber))

    (sink, source)
  }

}

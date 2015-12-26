package net.jetmq.broker

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.ws.{Message, BinaryMessage, UpgradeToWebsocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Sink, Source, Tcp}
import akka.util.ByteString


object BrokerServer {


  def run(system: ActorSystem) = {

    val bus = system.actorOf(Props[EventBusActor], name = "event-bus")
    val sessions = system.actorOf(Props(new SessionsManagerActor(bus)), name = "sessions")

    implicit val materializer = ActorMaterializer()(system)

    def requestHadler(connection: IncomingConnection, r: HttpRequest): HttpResponse = {
      r match {
        case req@HttpRequest(GET, Uri.Path("/mqtt"), _, _, _) =>
          req.header[UpgradeToWebsocket] match {
            case Some(upgrade) => {

              val handler = system.actorOf(Props(new TcpConnectionActor(sessions)).withMailbox("priority-dispatcher"),
                connection.remoteAddress.getHostString() + ":" + connection.remoteAddress.getPort())

              val sink: Sink[Message, Any] =
                Sink.foreach { (m: Message) => m match {
                  case BinaryMessage.Strict(data) => handler ! data
                  case x => {}
                }};

              upgrade.handleMessagesWithSinkSource(
                sink,
                Source(ActorPublisher[ByteString](handler)).map(x => BinaryMessage(x))
              )
            }
            case None => HttpResponse(400, entity = "Not a valid websocket request!")
          }
        case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")

      }
    }

    Http(system).bind(interface = "0.0.0.0", port = 8080)
      .to(Sink.foreach { connection =>
        println("Accepted new connection from " + connection.remoteAddress)

        val handler: (HttpRequest) => HttpResponse = r => requestHadler(connection, r)
        connection.handleWithSyncHandler(handler)
      })
      .run()


    Tcp(system).bind(interface = "0.0.0.0", port = 1883)
      .to(Sink.foreach { connection =>

        println("Accepted new connection from " + connection.remoteAddress)

        val handler = system.actorOf(Props(new TcpConnectionActor(sessions)).withMailbox("priority-dispatcher"),
          connection.remoteAddress.getHostString + ":" + connection.remoteAddress.getPort)

        val publisher = ActorPublisher[ByteString](handler)

        Source(publisher).via(connection.flow).to(Sink.foreach(handler ! _)).run()

      })
      .run()
  }
}

package net.jetmq.broker

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.ws.{BinaryMessage, UpgradeToWebsocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Sink, Source}

class ServerActor extends Actor with ActorLogging {

  import context.system

  val bus = system.actorOf(Props[EventBusActor], name = "event-bus")
  val sessions = system.actorOf(Props(new SessionsManagerActor(bus)), name = "sessions")

  implicit val materializer = ActorMaterializer()(context)

  def requestHadler(connection: IncomingConnection, r: HttpRequest): HttpResponse = {
    r match {
      case req@HttpRequest(GET, Uri.Path("/mqtt"), _, _, _) =>
        req.header[UpgradeToWebsocket] match {
          case Some(upgrade) => {

            val handler = system.actorOf(Props(new WsConnectionActor(sessions)), connection.remoteAddress.getHostString() + ":" + connection.remoteAddress.getPort())
            upgrade.handleMessagesWithSinkSource(
              Sink.foreach(handler ! _),
              Source(ActorPublisher[BinaryMessage](handler))
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

  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0", 1883))

  def receive = {

    case b@Bound(localAddress) => {
      log.info("bound to " + localAddress)
    }

    case CommandFailed(_: Bind) => {
      log.info("command failed")
      context stop self
    }

    case c@Connected(remote, local) => {

      log.info("client connected " + remote)

      val handler = system.actorOf(
        Props(new TcpConnectionActor(sessions)).withMailbox("priority-dispatcher"),
        remote.getHostString + ":" + remote.getPort)
      sender ! Register(handler)
    }
  }
}

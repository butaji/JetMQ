package net.jetmq.broker

import akka.actor._
import akka.http.scaladsl.model.ws.BinaryMessage
import akka.stream.actor.ActorPublisher
import akka.util.ByteString
import net.jetmq.broker.Helpers._
import scodec.Attempt.Failure

class WsConnectionActor (sessions: ActorRef) extends ActorPublisher[BinaryMessage] with ActorLogging {

  val mqtt = context.actorOf(Props(new MqttConnectionActor(sessions)))

  var i = 0

  def receive = {

    case BinaryMessage.Strict(data) => {

      log.info("" + data.toBitVector)

      val packets = PacketsHelper.decode(data.toBitVector)

      packets.foreach {
        case Left(p: Packet) => mqtt ! ReceivedPacket(p)
        case Right(p: Failure) => {
          log.warning("" + p)

          onError(new Throwable(p.cause.messageWithContext))
        }
      }
    }

    case SendingPacket(p) => {

      val bits = PacketsHelper.encode(p)

      onNext(BinaryMessage(ByteString(bits.require.toByteArray)))
    }

    case Closing => {
      onComplete()
    }

    case x => {

      log.error("Unexpected " + x)
    }
  }
}

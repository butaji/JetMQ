package net.jetmq.broker

import akka.actor._
import akka.http.scaladsl.model.ws.BinaryMessage
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.util.ByteString
import net.jetmq.broker.Helpers._
import scodec.Attempt.Failure

class WsConnectionActor (sessions: ActorRef) extends ActorPublisherWithBuffer[BinaryMessage] with ActorLogging {

  val mqtt = context.actorOf(Props(new MqttConnectionActor(sessions)))

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

    case Request(count) => {
      log.info("Requested " + count + " total demand " + totalDemand)
      deliverBuffer()
    }

    case Cancel => {
      log.info("Closing")
      context.stop(self)
    }

    case SendingPacket(p) => {

      val bits = PacketsHelper.encode(p)
      val envelope = BinaryMessage(ByteString(bits.require.toByteArray))

      onNextBuffered(envelope)
    }

    case Closing => {
      onComplete()
    }

    case x => {

      log.error("Unexpected " + x)
    }
  }
}

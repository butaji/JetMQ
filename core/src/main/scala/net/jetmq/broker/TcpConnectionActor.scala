package net.jetmq.broker

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.util.ByteString
import net.jetmq.broker.Helpers._
import scodec.Attempt.Failure

case class ReceivedPacket(packet: Packet)

case class SendingPacket(packet: Packet)

case object Closing

class TcpConnectionActor(sessions: ActorRef) extends ActorPublisherWithBuffer[ByteString] with ActorLogging {

  val mqtt = context.actorOf(Props(new MqttConnectionActor(sessions)))


  def receive = {

    case data: ByteString => {

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
      log.info("Requested: " + count + " demand is " + totalDemand + " and buffer is " + buffer.length)
      deliverBuffer()
    }

    case Cancel => {
      log.info("was canceled")
      onComplete()
      context.stop(self)
    }

    case SendingPacket(p) => {

      val bits = PacketsHelper.encode(p)
      val envelope = ByteString(bits.require.toByteArray)

      onNextBuffered(envelope)
    }

    case Closing => {
      onComplete()
      context.stop(self)
    }

    case x => {

      log.error("Unexpected " + x.getClass.getCanonicalName())
    }
  }
}

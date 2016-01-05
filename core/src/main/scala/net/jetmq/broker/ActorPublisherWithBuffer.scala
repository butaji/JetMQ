package net.jetmq.broker

import akka.stream.actor.ActorPublisher

import scala.annotation.tailrec

abstract trait ActorPublisherWithBuffer[T] extends ActorPublisher[T] {
  var buffer = Vector.empty[T]

  def onNextBuffered(envelope: T): Unit = {

    if (buffer.isEmpty && totalDemand > 0) {
      onNext(envelope)
    }
    else {
      buffer :+= envelope
    }
  }

  @tailrec final def deliverBuffer(): Unit =
    if (totalDemand > 0) {
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buffer.splitAt(totalDemand.toInt)
        buffer = keep
        use foreach onNext
      } else {
        val (use, keep) = buffer.splitAt(Int.MaxValue)
        buffer = keep
        use foreach onNext
        deliverBuffer()
      }
    }
}

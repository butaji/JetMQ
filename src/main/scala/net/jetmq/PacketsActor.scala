package net.jetmq.broker

import akka.actor.Actor
import akka.io.Tcp
import net.jetmq.Helpers._
import net.jetmq.packets.Packet
import scodec.Codec
import scodec.bits.BitVector

case class Decoded(packet: Packet)

case class Encoded(bytes: Tcp.Write)

case class DecodingError(exception: Throwable)

class PacketsActor extends Actor {

  def receive = {

    case b: BitVector => {

      try {
        val packet = Codec[Packet].decode(b).require.value
        sender ! Decoded(packet)
      }
      catch {
        case e:Throwable => sender ! DecodingError(e)
      }
    }

    case b: Packet => {

      val bytes = Codec[Packet].encode(b).toTcpWrite
      sender ! Encoded(bytes)
    }
  }
}

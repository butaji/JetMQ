package net.jetmq.broker

import akka.actor.Actor
import akka.io.Tcp
import net.jetmq.Helpers._
import net.jetmq.packets.Packet
import scodec.Codec
import scodec.bits.BitVector

case class Decoded(packet: Packet)

case class Encoded(bytes: Tcp.Write)

class PacketsActor extends Actor {

  def receive = {

    case b: BitVector => {

      val packet = Codec[Packet].decode(b).require.value
      sender ! Decoded(packet)
    }

    case b: Packet => {

      val bytes = Codec[Packet].encode(b).toTcpWrite
      sender ! Encoded(bytes)
    }
  }
}

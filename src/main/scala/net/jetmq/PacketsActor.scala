package net.jetmq.broker

import net.jetmq.packets.Packet
import scodec.{Attempt, Codec}
import scodec.bits.BitVector


object PacketsHelper {

  def decode(b: BitVector): Packet = {

    val packet = Codec[Packet].decode(b).require.value
    return packet
  }

  def encode(b: Packet): Attempt[BitVector] = {

    val bytes = Codec[Packet].encode(b)
    return bytes
  }
}

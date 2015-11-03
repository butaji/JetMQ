package net.jetmq.broker

import net.jetmq.packets.Packet
import scodec.{DecodeResult, Attempt, Codec}
import scodec.bits.BitVector


object PacketsHelper {

  def decode(b: BitVector): Attempt[DecodeResult[Packet]] = {

      val packet = Codec[Packet].decode(b)

    return packet

  }

  def encode(b: Packet): Attempt[BitVector] = {

    val bytes = Codec[Packet].encode(b)
    return bytes
  }
}

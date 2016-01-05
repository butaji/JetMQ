package net.jetmq.broker

import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult}

object PacketsHelper {

  def decode(b: BitVector): List[Packet] = {

      val packet = Codec[Packet].decode(b)

      packet match {
        case Successful(DecodeResult(p: Packet, r)) if r.length > 0 => {
          return p :: decode(r)
        }
        case Successful(DecodeResult(p: Packet, r)) if r.length == 0 => {
          return List(p)
        }
      }
  }

  def encode(b: Packet): Attempt[BitVector] = {

    val bytes = Codec[Packet].encode(b)
    return bytes
  }

  def format(l: List[Either[Packet, Failure]]): String = {
    l.map(format).mkString(", ")
  }

  def format(e: Either[Packet, Failure]): String = {
    e match {
      case Left(x) => {
        x.toString
      }
      case Right(f) => {
        f.toString
      }
    }
  }
}

package net.jetmq.broker

import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult}

/**
  * Created by vitalybaum on 21/11/15.
  */
object PacketsHelper {

  def decode(b: BitVector): List[Either[Packet, Failure]] = {

    try {
      val packet = Codec[Packet].decode(b)

      packet match {
        case Successful(DecodeResult(p: Packet, r)) if r.length > 0 => {
          return Left(p) :: decode(r)
        }
        case Successful(DecodeResult(p: Packet, r)) if r.length == 0 => {
          return List(Left(p))
        }
        case x:Failure => {
          return List(Right(x))
        }
      }
    } catch {
      case err:Throwable => return List(Right(Failure(scodec.Err(err.getMessage))))

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

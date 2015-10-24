package net.jetmq

import akka.io.Tcp
import akka.util.ByteString
import scodec.Attempt
import scodec.bits.BitVector

object Helpers {

  implicit class BitVectorHelper(val a: Attempt[BitVector]) extends AnyVal {

    def toTcpWrite(): Tcp.Write = {
      a.require.toByteArray.toTcpWrite
    }

    def toTcpReceived(): Tcp.Received ={
      a.require.toByteArray.toTcpReceived
    }
  }


  implicit class ByteArrayHelper(val a:Array[Byte]) extends AnyVal {

    def toBitVector(): BitVector = {
      BitVector(a)
    }

    def toTcpWrite(): Tcp.Write = {
      Tcp.Write(ByteString(a))
    }

    def toTcpReceived(): Tcp.Received = {
      Tcp.Received(ByteString(a))
    }

  }

  implicit class StringHelper(val s: String) extends AnyVal {

    def toBin(): Array[Byte] = {

      s.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
    }

    def toByteSring(): ByteString = {
      ByteString(s.toBin)
    }

    def toTcpReceived(): Tcp.Received = {

      Tcp.Received(s.toByteSring)

    }

    def toTcpWrite(): Tcp.Write = {
      Tcp.Write(s.toByteSring)
    }

  }


}

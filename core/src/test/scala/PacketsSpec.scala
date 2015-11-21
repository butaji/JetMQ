package net.jetmq.packets

import org.specs2.mutable._
import scodec.Codec
import scodec.bits._

class PacketsSpec extends Specification {

  "HEADER must be decoded with valid input" >> {
    Codec[Header].decode(bin"0000").require.value mustEqual Header(false, 0, false)
    Codec[Header].decode(bin"1000").require.value mustEqual Header(true, 0, false)

    Codec[Header].decode(bin"0010").require.value mustEqual Header(false, 1, false)
    Codec[Header].decode(bin"0100").require.value mustEqual Header(false, 2, false)

    Codec[Header].decode(bin"0001").require.value mustEqual Header(false, 0, true)
  }

  "HEADER must be encoded with valid input" >> {
    Codec[Header].encode(Header(true, 1, true)).require mustEqual bin"1011"
  }

  "CONNECT must be decoded with valid input" >> {
    Codec[Packet].decode(hex"100c00044d515454040200000000".bits).require.value mustEqual Connect(Header(false, 0, false), ConnectFlags(false, false, false, 0, false, true, 0), "")
  }

  "CONNACK must be decoded with valid input" >> {
    Codec[Packet].decode(hex"20020000".bits).require.value mustEqual Connack(Header(false, 0, false), 0)
  }

  "PUBLISH with empty body" >> {
    val p = Publish(Header(false, 0, false), "TopicA/B", 0, "")

    Codec[Packet].decode(hex"300a0008546f706963412f42".bits).require.value mustEqual (p)

    Codec[Packet].encode(p).require mustEqual (hex"300a0008546f706963412f42".bits)

  }
}


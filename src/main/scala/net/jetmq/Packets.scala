package net.jetmq.packets

import net.sigusr.mqtt.impl.frames.RemainingLengthCodec
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._
import shapeless._
import scodec.bits._

sealed trait Packet {
  def header: Header
}

case class Header(dup: Boolean, qos: Int, retain: Boolean) {
  assert(qos >= 0 && qos <= 2, "qos can't be outside [0,1,2]")
}

case class ConnectFlags(username: Boolean,
                        password: Boolean,
                        will_retain: Boolean,
                        will_qos: Int,
                        will_flag: Boolean,
                        clean_session: Boolean,
                        keep_alive: Int)

case class Connack(header: Header, return_code: Int) extends Packet

case class Connect(header: Header,
                   connect_flags: ConnectFlags,
                   client_id: String,
                   topic: Option[String] = None,
                   message: Option[String] = None,
                   user: Option[String] = None,
                   password: Option[String] = None) extends Packet

case class Publish(header: Header, topic: String, message_identifier: Int, payload: ByteVector) extends Packet

case class Puback(header: Header, message_identifier: Int) extends Packet

case class Pubrec(header: Header, message_identifier: Int) extends Packet

case class Pubrel(header: Header, message_identifier: Int) extends Packet

case class Pubcomp(header: Header, message_identifier: Int) extends Packet

case class Subscribe(header: Header, message_identifier: Int, topics: Vector[(String, Int)]) extends Packet

case class Suback(header: Header, message_identifier: Int, topics: Vector[Int]) extends Packet

case class Unsubscribe(header: Header, message_identifier: Int, topics: Vector[String]) extends Packet

case class Unsuback(header: Header, message_identifier: Int) extends Packet

case class Pingreq(header: Header) extends Packet

case class Pingresp(header: Header) extends Packet

case class Disconnect(header: Header) extends Packet


object Header {
  implicit val codec = (bool :: uint2 :: bool).as[Header]
}

object ConnectFlags {
  implicit val codec = (
    Codecs.connect_flags_prefix :~>:
      bool ::
      bool ::
      bool ::
      Codecs.qos ::
      bool ::
      bool ::
      ignore(1) :~>:
      Codecs.keep_alive).as[ConnectFlags]
}

object Codecs {
  val string = variableSizeBytes(uint16, utf8)
  val byte_padding = constant(bin"00000000")
  val connect_flags_prefix = constant(hex"00044d51545404".bits)
  val qos = uint2
  val return_code = uint16
  val message_id = uint16
  val keep_alive = uint16

  val remaining: Codec[Int] = new RemainingLengthCodec
}

object Packet {
  implicit val discriminated: Discriminated[Packet, Int] = Discriminated(uint4)
  implicit val codec = Codec.coproduct[Packet].auto
}

object Connect {
  implicit val discriminator: Discriminator[Packet, Connect, Int] = Discriminator(1)
  implicit val codec: Codec[Connect] = (Header.codec :: variableSizeBytes(Codecs.remaining,
    ConnectFlags.codec >>:~ { (hdr: ConnectFlags) ⇒
      Codecs.string ::
        conditional(hdr.will_flag, Codecs.string) ::
        conditional(hdr.will_flag, Codecs.string) ::
        conditional(hdr.username, Codecs.string) ::
        conditional(hdr.password, Codecs.string)
    })).as[Connect]
}


object Connack {
  implicit val discriminator: Discriminator[Packet, Connack, Int] = Discriminator(2)
  implicit val codec: Codec[Connack] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.return_code)).as[Connack]
}

object Publish {
  val dupLens = lens[Publish].header.dup
  implicit val discriminator: Discriminator[Packet, Publish, Int] = Discriminator(3)
  implicit val codec: Codec[Publish] = (Header.codec >>:~ {
    (hdr: Header) ⇒ variableSizeBytes(Codecs.remaining, Codecs.string :: (if (hdr.qos != 0) Codecs.message_id else provide(0)) :: bytes)
  }).as[Publish]
}

object Puback {
  implicit val discriminator: Discriminator[Packet, Puback, Int] = Discriminator(4)
  implicit val codec: Codec[Puback] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id)).as[Puback]
}

object Pubrec {
  implicit val discriminator: Discriminator[Packet, Pubrec, Int] = Discriminator(5)
  implicit val codec: Codec[Pubrec] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id)).as[Pubrec]
}

object Pubrel {
  val dupLens = lens[Pubrel].header.dup
  implicit val discriminator: Discriminator[Packet, Pubrel, Int] = Discriminator(6)
  implicit val codec: Codec[Pubrel] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id)).as[Pubrel]
}

object Pubcomp {
  implicit val discriminator: Discriminator[Packet, Pubcomp, Int] = Discriminator(7)
  implicit val codec: Codec[Pubcomp] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id)).as[Pubcomp]
}

object Subscribe {
  implicit val discriminator: Discriminator[Packet, Subscribe, Int] = Discriminator(8)
  val topicCodec: Codec[(String, Int)] = (Codecs.string :: ignore(6) :: uint2).dropUnits.as[(String, Int)]
  implicit val topicsCodec: Codec[Vector[(String, Int)]] = vector(topicCodec)
  implicit val codec: Codec[Subscribe] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id :: topicsCodec)).as[Subscribe]
}

object Suback {
  implicit val discriminator: Discriminator[Packet, Suback, Int] = Discriminator(9)
  implicit val qosCodec: Codec[Vector[Int]] = vector(ignore(6).dropLeft(uint2))
  implicit val codec: Codec[Suback] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id :: qosCodec)).as[Suback]
}

object Unsubscribe {
  implicit val discriminator: Discriminator[Packet, Unsubscribe, Int] = Discriminator(10)
  implicit val codec: Codec[Unsubscribe] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id :: vector(Codecs.string))).as[Unsubscribe]
}

object Unsuback {
  implicit val discriminator: Discriminator[Packet, Unsuback, Int] = Discriminator(11)
  implicit val codec: Codec[Unsuback] = (Header.codec :: variableSizeBytes(Codecs.remaining, Codecs.message_id)).as[Unsuback]
}

object Pingreq {
  implicit val discriminator: Discriminator[Packet, Pingreq, Int] = Discriminator(12)
  implicit val codec: Codec[Pingreq] = (Header.codec :: Codecs.byte_padding).dropUnits.as[Pingreq]
}

object Pingresp {
  implicit val discriminator: Discriminator[Packet, Pingresp, Int] = Discriminator(13)
  implicit val codec: Codec[Pingresp] = (Header.codec :: Codecs.byte_padding).dropUnits.as[Pingresp]
}

object Disconnect {
  implicit val discriminator: Discriminator[Packet, Disconnect, Int] = Discriminator(14)
  implicit val codec: Codec[Disconnect] = (Header.codec :: Codecs.byte_padding).dropUnits.as[Disconnect]
}

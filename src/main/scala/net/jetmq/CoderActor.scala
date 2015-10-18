package net.jetmq

import akka.actor.Actor
import net.jetmq.Helpers._
import net.jetmq.packets.{Disconnect, Header, Packet}
import scodec.Codec
import scodec.bits.BitVector

class CoderActor extends Actor {

  def receive = {

    case b: BitVector => {

      //val x:Connect = Codec[Connect].decode(hex"100c00044d515454040200000000".bits).require.value
    }

    case b: Packet => {

      val back = Disconnect(Header(false,0,false))
          sender ! Codec[Packet].encode(back).toTcpWrite
    }
  }
}

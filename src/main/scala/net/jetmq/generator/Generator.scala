package net.jetmq.generator

import net.jetmq.broker.PacketsHelper
import scodec.bits.BitVector
import net.jetmq.Helpers._

object Generator {

//   commented to not disturb sbt run

//  def main(args: Array[String]) {
//    run(args(0))
//  }

  def run(path: String) = {

    val lines = io.Source.fromFile(path).getLines.map(x => x.split('\t'))

    lines.filter(x => x.length > 5)
      .toList
      .groupBy(x => x(2).toInt + x(4).toInt - 1883)
      .toList
      .sortBy(x => x._1)
      .foreach(generateScenario)
  }

  def decode(p: String): String = {
    try {
      PacketsHelper.decode(BitVector(p.toBin)).require.value.toString()
    } catch {
      case _ => return "Broken package"
    }

  }

  def generateScenario(f: (Int, List[Array[String]])) = {
    println("\"Scenario " + f._1 + "\" in { ")
    println("   val h = create_actor(\"" + f._1 + "\")")

    f._2.foreach( x => {

      val p =x(5).replaceAll(":", "")
      if (x(2) != "1883") {

        println("")
        println("   h ! \"" + p + "\".toTcpReceived //" + decode(p))
      } else {

        println("   expectMsg(\"" + p + "\".toTcpWrite) //" + decode(p))
      }
    })


    println("   expectMsg(Tcp.Close)")
    println("   expectNoMsg(Bag.wait_time)")
    println("   success")
    println("}")
    println("")

  }
}

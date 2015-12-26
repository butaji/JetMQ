package net.jetmq.infra

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Actor, Props}
import akka.event.Logging._
import akka.pattern.BackoffSupervisor
import scala.concurrent.duration._

class LogstashTcpUploader extends Actor {

  val config = context.system.settings.config
  if (config.hasPath("logstash.active") && config.getBoolean("logstash.active")) {

    val address = new InetSocketAddress(config.getString("logstash.host"), config.getInt("logstash.port"))
    println("[Info] Enabling Logstash at " + address)

    val logstashProps = BackoffSupervisor.props(
      Props(new LogstashTcpConnection(address)),
      childName = "logstashConnection",
      minBackoff = 3.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.2)

    val logstash = context.actorOf(logstashProps)

    context.become(receive(logstash))
  } else {
    println("[Info] Logstash turned off")
    context.become(receive)

  }

  def receive = {
    case InitializeLogger(_)                        => sender() ! LoggerInitialized
    case m @ Error(cause, logSource, logClass, message) => {
      println(Console.RED + s"[Error] $cause $logSource $logClass $message" + Console.WHITE)
    }
    case m @ Warning(logSource, logClass, message)      => {
      println(Console.YELLOW + s"[Warn] $logSource $logClass $message" + Console.WHITE)
    }
    case m : PacketTrace          => {
      println(Console.GREEN + ((if(m.in) "->" else "<-") + " " + m.packet) + Console.WHITE)
    }
    case m @ Info(logSource, logClass, message)         => {
      println(s"[Info] $logSource $logClass $message")
    }
    case m @ Debug(logSource, logClass, message)        => {
      println(s"[Debug] $logSource $logClass $message")
    }
  }

  def receive(logstash: ActorRef):Receive = {
    case InitializeLogger(_)                        => sender() ! LoggerInitialized
    case m @ Error(cause, logSource, logClass, message) => {
      println(Console.RED + s"[Error] $cause $logSource $logClass $message" + Console.WHITE)

      logstash ! LogstashMessage("Error" ,logSource, logClass.getCanonicalName, message.toString, Some(cause.toString))
    }
    case m @ Warning(logSource, logClass, message)      => {
      println(Console.YELLOW + s"[Warn] $logSource $logClass $message" + Console.WHITE)

      logstash ! LogstashMessage("Warning" ,logSource, logClass.getCanonicalName, message.toString)
    }
    case m : PacketTrace          => {
      println(Console.GREEN + (if(m.in) "->" else "<-") + " " + m.packet + Console.WHITE)

      logstash ! m
    }
    case m @ Info(logSource, logClass, message)         => {
      println(s"[Info] $logSource $logClass $message")

      logstash ! LogstashMessage("Info" ,logSource, logClass.getCanonicalName, message.toString)
    }
    case m @ Debug(logSource, logClass, message)        => {
      println(s"[Debug] $logSource $logClass $message")

      logstash ! LogstashMessage("Debug" ,logSource, logClass.getCanonicalName, message.toString)
    }
  }
}

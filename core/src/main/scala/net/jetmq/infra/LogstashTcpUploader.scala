package net.jetmq.infra

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Actor, Props}
import akka.event.Logging._

class LogstashTcpUploader extends Actor {

  val config = context.system.settings.config
  if (config.hasPath("logstash.active") && config.getBoolean("logstash.active")) {

    val address = new InetSocketAddress(config.getString("logstash.host"), config.getInt("logstash.port"))
    println("[Info] Enabling Logstash at " + address)

    val logstash = context.actorOf(Props(new LogstashTcpConnection(address)))
    context.become(receive(logstash))
  } else {
    println("[Info] Logstash turned off")
    context.become(receive)

  }

  def receive = {
    case InitializeLogger(_)                        => sender() ! LoggerInitialized
    case m @ Error(cause, logSource, logClass, message) => {
      println(s"[Error] $cause $logSource $logClass $message")
    }
    case m @ Warning(logSource, logClass, message)      => {
      println(s"[Warn] $logSource $logClass $message")
    }
    case m : PacketTrace          => {
      println((if(m.in) "->" else "<-") + " " + m.packet)
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
      println(s"[Error] $cause $logSource $logClass $message")

      logstash ! LogstashMessage("Error" ,logSource, logClass.getCanonicalName, message.toString, Some(cause.toString))
    }
    case m @ Warning(logSource, logClass, message)      => {
      println(s"[Warn] $logSource $logClass $message")

      logstash ! LogstashMessage("Warning" ,logSource, logClass.getCanonicalName, message.toString)
    }
    case m : PacketTrace          => {
      println((if(m.in) "->" else "<-") + " " + m.packet)

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

package net.jetmq.infra

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.event.Logging._

class LogstashTcpUploader extends Actor {

  val address = new InetSocketAddress("logstash", 5000)
  val logstash = context.actorOf(Props(new LogstashTcpConnection(address)))

  def receive = {
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

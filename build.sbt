name := "JetMQ"

version := "0.1.0"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.6.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.0",
  "org.scodec" %% "scodec-core" % "1.7.1",
  "org.scalaz" %% "scalaz-core" % "7.1.1"
)

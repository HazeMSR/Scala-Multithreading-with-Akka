name := "scala-akka"

version := "0.1"

scalaVersion := "2.13.4"

val akkaVersion = "2.6.10"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
)
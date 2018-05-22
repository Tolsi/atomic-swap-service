name := "atomic-swap-service"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.14.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4"
libraryDependencies += "com.wavesplatform" % "wavesj" % "0.6"

resolvers += Resolver.mavenLocal
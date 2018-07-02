name := "atomic-swap-service"

version := "0.0.1"

scalaVersion := "2.12.6"

mainClass in assembly := Some("com.wavesplatform.atomicswap.Server")

libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.14.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4"
libraryDependencies += "com.wavesplatform" % "wavesj" % "0.8"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.12"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.12"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.2"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.2"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.1.2" % Test

lazy val root = (project in file(".")).dependsOn(scalaBitcoinJsonrpc)
lazy val scalaBitcoinJsonrpc = RootProject(uri("git://github.com/philwantsfish/scala-bitcoin-jsonrpc.git"))

resolvers += Resolver.mavenLocal
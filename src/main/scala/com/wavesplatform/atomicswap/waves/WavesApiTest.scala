package com.wavesplatform.atomicswap.waves

import akka.util.Timeout
import com.wavesplatform.atomicswap.Api

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object WavesApiTest extends App {
  val api = new WavesApi("https://nodes.wavesnodes.com/", confirmations = 5)
  implicit val timeout = Timeout(30 minutes)
  val f = api.waitForConfirmations("9wzgBpNomZmwYTFmZdhLmUfBbcN5cLxr1GjpxRqHkJYz")
  Await.result(f, timeout.duration)
  println(System.currentTimeMillis())
  Await.result(api.getTransaction("asd"), 1 minute)
  Await.result(api.terminate(), 1 minute)
}

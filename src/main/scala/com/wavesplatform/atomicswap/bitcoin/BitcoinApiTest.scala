package com.wavesplatform.atomicswap.bitcoin

import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object BitcoinApiTest extends App {
  val api = new BitcoinApi("http://test:test@127.0.0.1:18332/", confirmations = 1)
  implicit val timeout = Timeout(30 minutes)
  val f = api.waitForConfirmations("e23260244b54c9a9963f1746fa9e5b00035454efffc1ca980271b0b48a8e9b61")
  Await.result(f, timeout.duration)
  println(System.currentTimeMillis())
  Await.result(api.getTransaction("asd"), 10 seconds)
  Await.result(api.terminate(), 1 minute)
}

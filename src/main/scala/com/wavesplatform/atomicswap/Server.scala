package com.wavesplatform.atomicswap

import com.wavesplatform.atomicswap.bitcoin.BitcoinApi
import com.wavesplatform.atomicswap.waves.WavesApi
import scala.concurrent.ExecutionContext.Implicits.global

object Server extends App {
  val bitcoinApi = new BitcoinApi("http://test:test@127.0.0.1:18332/")
  val wavesApi = new WavesApi("https://pool.testnet.wavesnodes.com/")
  new HttpService(wavesApi, bitcoinApi).startServer("localhost", 8080)
}

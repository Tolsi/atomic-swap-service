package com.wavesplatform.atomicswap.waves

import akka.util.Timeout
import com.wavesplatform.atomicswap.atomicexchange.ServiceSide
import com.wavesplatform.atomicswap.{ConsoleFakeNetwork, ExchangeParams}
import com.wavesplatform.wavesj.{Node, PrivateKeyAccount, PublicKeyAccount}
import org.bitcoinj.core.{Coin, Sha256Hash}
import org.bitcoinj.params.TestNet3Params

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object WavesApiTest extends App {
  val api = new WavesApi("https://testnode1.wavesnodes.com", confirmations = 1, tryEvery = 10 seconds)
  val n = new Node("https://testnode1.wavesnodes.com")
  implicit val timeout = Timeout(30 minutes)
  //  val f = api.waitForConfirmations("9wzgBpNomZmwYTFmZdhLmUfBbcN5cLxr1GjpxRqHkJYz")
  //  Await.result(f, timeout.duration)
  println(System.currentTimeMillis())
  Await.result(api.getTransaction("asd"), 1 minute)

  val serviceX = "I don't like alice".getBytes

  implicit private val p: ExchangeParams = ExchangeParams(
    TestNet3Params.get(),
    'T',
    // 0.01 BTC
    bitcoinFee = Coin.CENT,
    minutesTimeout = 30 minutes,
    wavesBlocksTimeout = 30,
    10,
    Coin.parseCoin("1.09952659"),
    wavesFee = 100000,
    wavesSmartFee = 500000,
    Sha256Hash.hash(serviceX),
    1533645443L.seconds,
    ConsoleFakeNetwork,
    344752)

  // 3N2Bk9EtqW5PGuc38pYaEUKzqsMjEydwXFi
  private val wavesUser =
    PrivateKeyAccount.fromSeed("sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decrease", 0, p.wavesNetwork.toByte)
  private val wavesUserTmpPrivateKey =
    PrivateKeyAccount.fromSeed("sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decreassrr34gg48", 0, p.wavesNetwork.toByte)
  private val wavesUserTmpPublicKey = new PublicKeyAccount(wavesUserTmpPrivateKey.getPublicKey, p.wavesNetwork.toByte)

  private val bitcoinUserWavesAccount = PrivateKeyAccount.fromSeed(
    "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author trololo!", 1, p.wavesNetwork.toByte)

  // TX1 - Alice Waves -> scr1 money to tmp account + TX1-1 fee  + TX3 fee
  // TX1-1 - scr1 set script to tmp account

  val tx1 = WavesSide.sendMoneyToTempSwapAccount(wavesUser, wavesUserTmpPrivateKey, p.wavesFee)
  val tx1_1 = WavesSide.setSwapScriptOnTempSwapAccount(n, wavesUserTmpPrivateKey, wavesUser, bitcoinUserWavesAccount.getAddress, p.currentWavesHeight + 30, p.wavesFee)

  // TX4 - Alice [normal case] - scr1 -> Alice Bitcoin address

  //  val tx4 = ServiceSide.accomplishWavesSwapTransaction(wavesUserTmpPublicKey, bitcoinUserWavesAccount, serviceX, p.wavesSmartFee)

  // TX6 - Bob [failed case] - scr2 (TX1) -> Bob Bitcoin address
  val tx6 = ServiceSide.recoverWavesSwapTransaction(wavesUserTmpPublicKey, wavesUser, p.wavesSmartFee)

  //  println(tx1.id)
  //  println(tx1_1.id)
  //  println(tx4.id)
  println(tx6.id)

  Await.result(api.sendAndWaitForConfirmations(tx1), 30 minutes)
  Await.result(api.sendAndWaitForConfirmations(tx1_1), 30 minutes)
  //  Await.result(api.sendAndWaitForConfirmations(tx4), 30 minutes)
  Await.result(api.sendAndWaitForConfirmations(tx6), 30 minutes)

  Await.result(api.terminate(), 1 minute)
}

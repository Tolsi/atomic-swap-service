package com.wavesplatform.atomicswap.bitcoin

import akka.util.Timeout
import com.wavesplatform.atomicswap.atomicexchange.ServiceSide
import com.wavesplatform.atomicswap.bitcoin.coinswap.BitcoinSide
import com.wavesplatform.atomicswap.bitcoin.util.KeysUtil
import com.wavesplatform.atomicswap.{ConsoleFakeNetwork, ExchangeParams}
import org.bitcoinj.core.{Coin, ECKey, Sha256Hash}
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.ScriptBuilder

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

  private val serviceX = "I don't like alice".getBytes
  implicit private val p: ExchangeParams = ExchangeParams(
    TestNet3Params.get(),
    'T',
    // 0.01 BTC
    bitcoinFee = Coin.CENT,
    minutesTimeout = 30 minutes,
    wavesBlocksTimeout = 30,
    10,
    Coin.parseCoin("0.85883"),
    wavesFee = 100000,
    wavesSmartFee = 500000,
    Sha256Hash.hash(serviceX),
    1533645443L.seconds,
    ConsoleFakeNetwork,
    344706)

  private val wavesUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(
    "91jVRNUJWu9ousWk6LdeUFRPcdFDmBiw5jnpYLogE2Ki8AwiZQg"))

  // mxxk5mdpPUrpAuxA4JC3oEbUeZ6EHUFa6f
  private val bitcoinUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(
    "91h8oWwuCkzxs979qFNXLF9raNxewMYozW2MnT9pPJ8mids26Wi"))

  // todo many outputs support
  private val bitcoinUserBitcoinOutInfo = BitcoinInputInfo("47fec4375b78b398e8d7dbbb0921574b48bbfbd8eb871a5c9de5b889bece5664", 1,
    ScriptBuilder.createOutputScript(bitcoinUserBitcoinECKey.toAddress(p.bitcoinNetworkParams)), bitcoinUserBitcoinECKey.getPrivKeyBytes)


  val tx2 = BitcoinSide.createAtomicSwapTransaction(bitcoinUserBitcoinOutInfo, bitcoinUserBitcoinECKey.getPubKey, wavesUserBitcoinECKey.getPubKey, p.startTimestamp.toSeconds + p.minutesTimeout.toSeconds)

  //  val tx3 = ServiceSide.accomplishBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, serviceX, wavesUserBitcoinECKey.getPrivKeyBytes, wavesUserBitcoinECKey.getPubKey)

  val tx5 = ServiceSide.recoverBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, p.bitcoinAmount.minus(p.bitcoinFee.multiply(2)), bitcoinUserBitcoinECKey.getPrivKeyBytes, bitcoinUserBitcoinECKey.getPubKey)

  println(tx2.id)
  //  println(tx3.id)
  println(tx5.id)

  Await.result(api.sendAndWaitForConfirmations(tx2), 30 minutes)
  //  Await.result(api.sendAndWaitForConfirmations(tx3), 30 minutes)
  Await.result(api.sendAndWaitForConfirmations(tx5), 30 minutes)

  Await.result(api.terminate(), 1 minute)
}

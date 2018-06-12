package com.wavesplatform.atomicswap

import com.wavesplatform.atomicswap.atomicexchange.ServiceSide
import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import com.wavesplatform.atomicswap.bitcoin.coinswap.BitcoinSide
import com.wavesplatform.atomicswap.bitcoin.util.KeysUtil
import com.wavesplatform.atomicswap.waves.WavesSide
import com.wavesplatform.wavesj._
import org.bitcoinj.core.{Coin, ECKey, Sha256Hash}
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.ScriptBuilder

import scala.concurrent.duration._

object Playground extends App {

  def buildTestExchangeParams(
                               hashX: Array[Byte],
                               wavesAmount: Int,
                               bitcoinAmount: Coin,
                               startTimestamp: FiniteDuration,
                               currentWavesHeight: Int
                             ): ExchangeParams = ExchangeParams(
    TestNet3Params.get(),
    // 0.01 BTC
    bitcoinFee = Coin.CENT,
    minutesTimeout = 30 minutes,
    wavesBlocksTimeout = 30,
    wavesAmount,
    bitcoinAmount,
    wavesFee = 100000,
    wavesSmartFee = 500000,
    hashX,
    startTimestamp,
    ConsoleFakeNetwork,
    currentWavesHeight)

  // 3N2Bk9EtqW5PGuc38pYaEUKzqsMjEydwXFi
  private val wavesUser =
    PrivateKeyAccount.fromSeed(Base58.encode(
      "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decrease".getBytes), 1, 'T')
  private val wavesUserTmpPrivateKey =
    PrivateKeyAccount.fromSeed(Base58.encode(
      "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decreassemls".getBytes), 1, 'T')
  private val wavesUserTmpPublicKey = new PublicKeyAccount(wavesUserTmpPrivateKey.getPublicKey, 'T')
  // mzqD1A9pAJyELfMAWUrqcz5K8WfQEcXTPY
  private val wavesUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(
    "91jVRNUJWu9ousWk6LdeUFRPcdFDmBiw5jnpYLogE2Ki8AwiZQg"))

  // mxxk5mdpPUrpAuxA4JC3oEbUeZ6EHUFa6f
  private val bitcoinUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(
    "91h8oWwuCkzxs979qFNXLF9raNxewMYozW2MnT9pPJ8mids26Wi"))
  private val bitcoinUserWavesAccount = PrivateKeyAccount.fromSeed(Base58.encode(
    "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author trololo!".getBytes), 1, 'T')

  private val serviceX = "I don't like alice".getBytes

  implicit private val p = buildTestExchangeParams(
    Sha256Hash.hash(serviceX),
    10,
    Coin.parseCoin("1.28"),
    1528814953L.seconds,
    372475
  )

  // todo many outputs support
  private val bitcoinUserBitcoinOutInfo = BitcoinInputInfo("04891274037778941a771bcaad74569a8f5ca53b447acbbd711f5c7b25d70a1c", 0,
    ScriptBuilder.createOutputScript(bitcoinUserBitcoinECKey.toAddress(p.networkParams)), bitcoinUserBitcoinECKey.getPrivKeyBytes)

  // TX1 - Alice Waves -> scr1 money to tmp account + TX0-1 fee
  // TX1-1 - scr1 set script to tmp account

  val n = new Node("https://testnode1.wavesnodes.com/")

  val tx1 = WavesSide.sendMoneyToTempSwapAccount(wavesUser, wavesUserTmpPrivateKey, p.wavesFee)
  val tx1_1 = WavesSide.setSwapScriptOnTempSwapAccount(n, wavesUserTmpPrivateKey, wavesUser, bitcoinUserWavesAccount.getAddress, p.currentWavesHeight + 30, p.wavesFee)

  // TX2 - Bob Bitcoin -> scr2

  val tx2 = BitcoinSide.createAtomicSwapTransaction(bitcoinUserBitcoinOutInfo, bitcoinUserBitcoinECKey.getPubKey, wavesUserBitcoinECKey.getPubKey, p.startTimestamp.toSeconds + p.minutesTimeout.toSeconds)

  // TX3 - Service [normal case] - scr1 -> Bob Waves address
  // TX4 - Service [normal case] - scr1 -> Alice Bitcoin address

  val tx3 = ServiceSide.accomplishBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, serviceX, wavesUserBitcoinECKey.getPrivKeyBytes, wavesUserBitcoinECKey.getPubKey)
  val tx4 = ServiceSide.accomplishWavesSwapTransaction(wavesUserTmpPublicKey, bitcoinUserWavesAccount, serviceX, p.wavesSmartFee)

  // TX5 - Service (or someone) [failed case] - scr1 (TX0-1) -> Alice Waves address
  // TX6 - Service (or someone) [failed case] - scr2 (TX1) -> Bob Bitcoin address

  val tx5 = ServiceSide.recoverBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, p.bitcoinAmount.minus(p.bitcoinFee.multiply(2)), bitcoinUserBitcoinECKey.getPrivKeyBytes, bitcoinUserBitcoinECKey.getPubKey)
  val tx6 = ServiceSide.recoverWavesSwapTransaction(wavesUserTmpPublicKey, wavesUser, p.wavesSmartFee)

  println("bytes")
  Seq(tx1, tx1_1, tx2, tx3, tx4, tx5, tx6).map(_.bytesString).foreach(println)
  println("ids")
  Seq(tx1, tx1_1, tx2, tx3, tx4, tx5, tx6).map(_.id).foreach(println)
}

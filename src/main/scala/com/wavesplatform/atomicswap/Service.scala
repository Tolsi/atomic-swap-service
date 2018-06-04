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

object Service extends App {

  def buildTestExchangeParams(
                               hashX: Array[Byte],
                               wavesAmount: Int,
                               bitcoinAmount: Coin,
                               now: Long,
                               currentWavesHeight: Int
                             ): ExchangeParams = ExchangeParams(
    TestNet3Params.get(),
    // 0.01 BTC
    fee = Coin.CENT,
    timeout = 1 minute,
    wavesAmount,
    bitcoinAmount,
    hashX,
    now,
    ConsoleFakeNetwork,
    currentWavesHeight)

  // 3N2Bk9EtqW5PGuc38pYaEUKzqsMjEydwXFi
  private val wavesUser =
    PrivateKeyAccount.fromSeed(Base58.encode(
      "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decrease".getBytes), 1, 'T')
  private val wavesUserTmpPrivateKey =
    PrivateKeyAccount.fromSeed(Base58.encode(
      "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decrease1fg".getBytes), 1, 'T')
  private val wavesUserTmpPublicKey = new PublicKeyAccount(wavesUserTmpPrivateKey.getPublicKey, 'T')
  // empty mzqD1A9pAJyELfMAWUrqcz5K8WfQEcXTPY
  private val wavesUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(
    "91jVRNUJWu9ousWk6LdeUFRPcdFDmBiw5jnpYLogE2Ki8AwiZQg"))

  // 4.69917212 n2DJhSo1AnGU95gdt6GVGLcnPhBmLnMz9U
  private val bitcoinUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(
    "91h8oWwuCkzxs979qFNXLF9raNxewMYozW2MnT9pPJ8mids26Wi"))
  private val bitcoinUserWavesAccount = PrivateKeyAccount.fromSeed(Base58.encode(
    "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author trololo!".getBytes), 1, 'T')

  private val serviceX = "I don't like alice".getBytes
  // 3N3kaihRJ5bwSRJKnePGsHDFxCgVA7zCbEE
  private val serviceWavesPrivateKey = PrivateKeyAccount.fromSeed(Base58.encode(
    "sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author Gewgwegf3d".getBytes), 1, 'T')
  private val serviceWavesPublicKey = new PublicKeyAccount(serviceWavesPrivateKey.getPublicKey, 'T')
  //  3.09898185  n2FMUoJQ6BWRJeWqjAPk8LdsC98Bkt26JQ
  private val serviceBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(
    "92NJU1S96ZKwhtVGmHkMWZZsyWwZeKmYUwjCvTmkCNMELhA6HQe"))

  private val currentWavesHeight: Int = 372665
//  private val now = System.currentTimeMillis()
  private val now = 1527696585240L

  println(now)

  implicit private val p = buildTestExchangeParams(
    Sha256Hash.hash(serviceX),
    10,
    Coin.parseCoin("1.3"),
    now,
    currentWavesHeight
  )

  // todo many outputs support
  private val bitcoinUserBitcoinOutInfo = BitcoinInputInfo("6d932c2d07ed8e752656d95e6391e827188ba6dda1883a253c4df45aedef2886", 0,
    ScriptBuilder.createOutputScript(bitcoinUserBitcoinECKey.toAddress(p.networkParams)), bitcoinUserBitcoinECKey.getPrivKeyBytes)

  // TX1 - Alice Waves -> scr1 money to tmp account + TX0-1 fee
  // TX1-1 - scr1 set script to tmp account

  val tx1 = WavesSide.sendMoneyToTempSwapAccount(wavesUser, wavesUserTmpPrivateKey, 100000)
  val tx1_1 = WavesSide.setSwapScriptOnTempSwapAccount(wavesUserTmpPrivateKey, wavesUser, bitcoinUserWavesAccount.getAddress, p.currentWavesHeight + 30, 100000)

  // TX2 - Bob Bitcoin -> scr2

  val tx2 = BitcoinSide.createAtomicSwapTransaction(bitcoinUserBitcoinOutInfo, bitcoinUserBitcoinECKey.getPubKey, wavesUserBitcoinECKey.getPubKey, p.startTimestampMillis / 1000 + 30.minutes.toSeconds)

  // TX3 - Service [normal case] - scr1 -> Bob Waves address
  // TX4 - Service [normal case] - scr1 -> Alice Bitcoin address

  val tx3 = ServiceSide.accomplishBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, serviceX, serviceBitcoinECKey.getPrivKeyBytes, bitcoinUserBitcoinECKey.getPubKey)
  val tx4 = ServiceSide.accomplishWavesSwapTransaction(serviceWavesPublicKey, wavesUser, serviceX, 400000)

  // TX5 - Service (or someone) [failed case] - scr1 (TX0-1) -> Alice Waves address
  // TX6 - Service (or someone) [failed case] - scr2 (TX1) -> Bob Bitcoin address

  val tx5 = ServiceSide.recoverBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, p.bitcoinAmount, serviceBitcoinECKey.getPrivKeyBytes, bitcoinUserBitcoinECKey.getPubKey)
  val tx6 = ServiceSide.recoverWavesSwapTransaction(wavesUserTmpPublicKey, wavesUser, 400000)

  Seq(tx1, tx1_1, tx2, tx3, tx4, tx5, tx6).map(_.bytesString).foreach(println)
}

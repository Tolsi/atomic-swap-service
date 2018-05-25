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

  private val wavesUser =
    PrivateKeyAccount
      .fromSeed(Base58.encode("sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decrease".getBytes), 1, 'T')
  private val wavesUserTmpPrivateKey =
    PrivateKeyAccount
      .fromSeed(Base58.encode("sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author decrease1fg".getBytes), 1, 'T')
  private val wavesUserBitcoinPublicKey = Base58.decode("???")

  private val bitcoinUser = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF("???"))
  private val bitcoinUserWavesPublicKey = new PublicKeyAccount(Base58.decode("???"), 'T')

  private val serviceX = "I don't like alice".getBytes
  private val serviceWavesPrivateKey = PrivateKeyAccount.fromSeed(Base58.encode("sad capable gospel wage bean evoke hundred crawl logic question cheese outer leader author Gewgwegf3d".getBytes), 1, 'T')
  private val serviceWavesPublicKey = new PublicKeyAccount(serviceWavesPrivateKey.getPublicKey, 'T')
  private val serviceBitcoinPrivateKey = Base58.decode("???")

  private val currentWavesHeight: Int = ???

  implicit private val p = buildTestExchangeParams(
    Sha256Hash.hash(serviceX),
    10,
    Coin.parseCoin("1.3"),
    System.currentTimeMillis() / 1000,
    currentWavesHeight
  )

  // todo many outputs support
  private val bitcoinUserBitcoinOutInfo = BitcoinInputInfo("6d932c2d07ed8e752656d95e6391e827188ba6dda1883a253c4df45aedef2886", 0,
    ScriptBuilder.createOutputScript(bitcoinUser.toAddress(p.networkParams)), bitcoinUser.getPrivKeyBytes)

  // TX1 - Alice Waves -> scr1 money to tmp account + TX0-1 fee
  // TX1-1 - scr1 set script to tmp account

  val tx1 = WavesSide.sendMoneyToTempSwapAccount(wavesUser, wavesUserTmpPrivateKey, 100000)
  val tx1_1 = WavesSide.setSwapScriptOnTempSwapAccount(wavesUserTmpPrivateKey, wavesUser, bitcoinUserWavesPublicKey.getAddress, p.currentWavesHeight + 30, 100000)

  // TX2 - Bob Bitcoin -> scr2

  val tx2 = BitcoinSide.createAtomicSwapTransaction(bitcoinUserBitcoinOutInfo, bitcoinUser.getPubKey, wavesUserBitcoinPublicKey, p.startTimestampSeconds + 30.minutes.toSeconds)

  // TX3 - Service [normal case] - scr1 -> Bob Waves address
  // TX4 - Service [normal case] - scr1 -> Alice Bitcoin address

  val tx3 = ServiceSide.accomplishBitcoinSwapTransaction(tx2.getHashAsString, tx2.getOutput(0).getScriptPubKey, serviceX, serviceBitcoinPrivateKey, bitcoinUser.getPubKey)
  val tx4 = ServiceSide.accomplishWavesSwapTransaction(serviceWavesPublicKey, wavesUser, serviceX, 400000)

  // TX5 - Service (or someone) [failed case] - scr1 (TX0-1) -> Alice Waves address
  // TX6 - Service (or someone) [failed case] - scr2 (TX1) -> Bob Bitcoin address

  val tx5 = ServiceSide.recoverBitcoinSwapTransaction(tx2.getHashAsString, tx2.getOutput(0).getScriptPubKey, p.bitcoinAmount, serviceBitcoinPrivateKey, bitcoinUser.getPubKey)
  val tx6 = ServiceSide.recoverWavesSwapTransaction(serviceWavesPublicKey, wavesUser, 400000)
}

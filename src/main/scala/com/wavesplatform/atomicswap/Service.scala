package com.wavesplatform.atomicswap

import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo

import scala.concurrent.duration._
import com.wavesplatform.atomicswap.bitcoin.util.KeysUtil
import com.wavesplatform.atomicswap.waves.AtomicSwapScriptSetScriptTransactionBuilder
import com.wavesplatform.wavesj._
import org.bitcoinj.core.{Coin, ECKey, Sha256Hash, Utils}
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.ScriptBuilder

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

  private val serviceBitcoinPublicKey = Base58.decode("???")

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
  private val serviceWavesPublicKey = new PublicKeyAccount(Base58.decode("???"), 'T')

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
  val tx1 = Transaction.makeTransferTx(wavesUser, wavesUserTmpPrivateKey.getAddress, p.wavesAmount, Asset.WAVES, 100000, Asset.WAVES, "")
  val wavesSwapScript = AtomicSwapScriptSetScriptTransactionBuilder.build(bitcoinUserWavesPublicKey.getAddress, wavesUser.getAddress, wavesUser,
    p.hashX, currentWavesHeight + 30, wavesUserTmpPrivateKey, 100000, p.startTimestamp)
  val tx11 = Transaction.makeScriptTx(wavesUserTmpPrivateKey, Base58.encode(wavesSwapScript), 'T', 100000)
  // TX2 - Bob Bitcoin -> scr2

  // TX3 - Service [normal case] - scr1 -> Bob Waves address
  // TX4 - Service [normal case] - scr1 -> Alice Bitcoin address

  // TX5 - Service (or someone) [failed case] - scr1 (TX0-1) -> Alice Waves address
  // TX6 - Service (or someone) [failed case] - scr2 (TX1) -> Bob Bitcoin address

//  val (alice1, atc1) = Alice.step1(alicePk, aliceOutInfoBC1, carolPublic).right.get
//  val (carol2, cta1, ctb1) = Carol.step2(carolPk, carolOutInfoBC2, alicePublic, alicePublic, atc1).right.get
//  val (bob3, btc1) = Bob.step3(alicePk, bobX, carolPublic, ctb1).right.get
//  // broadcast tx0 and tx1
//  val alice4 = Alice.step4(alicePk, cta1, alice1).right.get
//  val carol5 = Carol.step5(carolPk, carol2, btc1).right.get
//  // wait for tx0 and tx1, X
//  val (carol6, ctb2) = Carol.step6(carolPk, carol5, bobX).right.get
//  Bob.step7(alicePk, bob3, ctb2).right.get
//  val atc2 = Alice.step8(alicePk, alice4).right.get
//  Carol.step9(carolPk, carol6, atc2).right.get
//  logger.info("Done")
}

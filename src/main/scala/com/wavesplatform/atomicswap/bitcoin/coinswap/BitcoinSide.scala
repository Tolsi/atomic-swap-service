//package com.wavesplatform.atomicswap.bitcoin.coinswap
//
//import com.typesafe.scalalogging.StrictLogging
//import com.wavesplatform.atomicswap.ExchangeParams
//import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
//import org.bitcoinj.core.{ECKey, Transaction}
//import org.bitcoinj.script.ScriptBuilder
//import org.spongycastle.util.encoders.Hex
//import com.wavesplatform.atomicswap.bitcoin.util.TransactionsUtil._
//
//import scala.collection.JavaConverters._
//
//trait PublicState {
//  def publicKey: Array[Byte]
//}
//
//trait WavesUserPublicState extends PublicState
//
//trait BitcoinUserPublicState extends PublicState
//
//case class WavesUserAfter1Step(override val publicKey: Array[Byte],
//                           carolPublicState: CarolPublicState,
//                           TX0: Transaction,
//                           wavesUserTX2signature: BitcoinInputInfo) extends WavesUserPublicState
//
//case class WavesUserAfter4Step(override val publicKey: Array[Byte],
//                           prevState: WavesUserAfter1Step,
//                           TX2: Transaction) extends WavesUserPublicState
//
//case class MessageWavesUserToBobAfter1Step(
//                                        TX0Id: String,
//                                        wavesUserTX2signature: BitcoinInputInfo)
//
//case class MessageWavesUserToCarolAfter8Step(
//                                          wavesUserTx4Signature: BitcoinInputInfo)
//
//object BitcoinSide extends StrictLogging {
//  def step1(bitcoinUserPrivateKey: Array[Byte],
//            bitcoinUserInput: BitcoinInputInfo,
//            otherNetworkOpponentPublicState: CarolPublicState)(implicit p: ExchangeParams): Either[Exception, (WavesUserAfter1Step, MessageWavesUserToBobAfter1Step)] = {
//    val wavesUserPublicKey = ECKey.fromPrivate(bitcoinUserPrivateKey).getPubKey
//
//    val TX0script = createXHashUntilTimelockOrToSelfScript(p.hashX, prevState.carolPublicState.publicKey, lockTimeWavesUserTs, prevState.publicKey)
//
//    val TX0Amount = p.wavesUserAmount.minus(p.fee)
//    val TX0 = sendMoneyToScript(bitcoinUserInput, TX0Amount, TX0script)
//    val TX0id = TX0.getHashAsString
//
//    val wavesUserTX2signature = BitcoinInputInfo(TX0id, 0, multisigScript2of2BC1, bitcoinUserPrivateKey)
//
//    Right(WavesUserAfter1Step(wavesUserPublicKey, otherNetworkOpponentPublicState, TX0, wavesUserTX2signature), MessageWavesUserToBobAfter1Step(TX0id, wavesUserTX2signature))
//  }
//}

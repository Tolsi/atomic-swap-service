package com.wavesplatform.atomicswap

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{HttpApp, Route}
import com.google.common.base.Charsets
import com.wavesplatform.atomicswap.atomicexchange.ServiceSide
import com.wavesplatform.atomicswap.bitcoin.coinswap.BitcoinSide
import com.wavesplatform.atomicswap.bitcoin.util.KeysUtil
import com.wavesplatform.atomicswap.bitcoin.{BitcoinApi, BitcoinInputInfo}
import com.wavesplatform.atomicswap.waves.{WavesApi, WavesSide}
import com.wavesplatform.wavesj.{Node, PrivateKeyAccount, PublicKeyAccount}
import org.bitcoinj.core.{Coin, ECKey, NetworkParameters, Sha256Hash}
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.ScriptBuilder
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  def bitcoinNetworkParameters: NetworkParameters

  implicit val bitcoinInputInfoFormat = new JsonFormat[BitcoinInputInfo] {
    override def read(json: JsValue): BitcoinInputInfo = {
      val asObj = json.asJsObject
      val txId = asObj.fields("txId").convertTo[String]
      val outputIndex = asObj.fields("outputIndex").convertTo[Int]
      val bitcoinUserPrivateKeyWIF = asObj.fields("bitcoinUserPrivateKeyWIF").convertTo[String]
      val bitcoinUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(bitcoinUserPrivateKeyWIF))
      BitcoinInputInfo(txId, outputIndex, ScriptBuilder.createOutputScript(bitcoinUserBitcoinECKey.toAddress(bitcoinNetworkParameters)), bitcoinUserBitcoinECKey.getPrivKeyBytes)
    }

    override def write(obj: BitcoinInputInfo): JsValue = ???
  }
  implicit val startExchangeDemoRequestFormat = jsonFormat9(StartExchangeDemoRequest.apply)
}


case class StartExchangeDemoRequest(
                                     secret: String,
                                     wavesAmount: Long,
                                     bitcoinAmount: Long,
                                     wavesUserWavesPrivateKey: String,
                                     wavesTmpPrivateKey: String,
                                     wavesUserBitcoinPrivateKeyWIF: String,
                                     bitcoinUserWavesPublicKey: String,
                                     bitcoinInputInfo: BitcoinInputInfo,
                                     startTimestampMillis: Long)

object HttpService {
  def processTransactions(wavesApi: WavesApi, bitcoinApi: BitcoinApi,
                          tx1: WavesTransferTransaction,
                          tx1_1: WavesTransferTransaction,
                          tx2: BitcoinTransferTransaction,
                          tx3: BitcoinTransferTransaction,
                          tx4: WavesTransferTransaction,
                          tx5: BitcoinTransferTransaction,
                          tx6: WavesTransferTransaction
                         )(implicit timeout: akka.util.Timeout): Future[Either[String, String]] = {

    def initStep1F = Future.sequence(Seq(wavesApi.sendAndWaitForConfirmations(tx1), bitcoinApi.sendAndWaitForConfirmations(tx2)))

    def initStep2F = wavesApi.sendAndWaitForConfirmations(tx1_1)

    def successF = Future.sequence(Seq(wavesApi.sendAndWaitForConfirmations(tx4), bitcoinApi.sendAndWaitForConfirmations(tx3)))

    def failureF = Future.sequence(Seq(wavesApi.sendAndWaitForConfirmations(tx6), bitcoinApi.sendAndWaitForConfirmations(tx5)))

    println("1")
    initStep1F.flatMap(_ => {
      println("2")
      initStep2F
    }).flatMap(_ => {
      println("3")
      successF.map(_ => {
        println("4")
        Right("Successful!")
      })
    }).recoverWith { case _ =>
      // todo fallback only applied txs
      failureF.map(_ => Left("Fallback successful!")).fallbackTo(Future.successful(Left("Fallback failed, but recover completed")))
    }
  }
}

class HttpService(wavesApi: WavesApi, bitcoinApi: BitcoinApi) extends HttpApp with JsonSupport {

  override val bitcoinNetworkParameters: NetworkParameters = TestNet3Params.get()
  val wavesNetwork = 'T'

  def buildTestExchangeParams(
                               hashX: Array[Byte],
                               wavesAmount: Int,
                               bitcoinAmount: Coin,
                               startTimestamp: FiniteDuration,
                               currentWavesHeight: Int
                             ): ExchangeParams = ExchangeParams(
    bitcoinNetworkParameters,
    wavesNetwork,
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

  val wavesNode = new Node("https://testnode1.wavesnodes.com/")

  override protected def routes: Route = {
    pathPrefix("exchange") {
      path("demo") {
        post {
          entity(as[StartExchangeDemoRequest]) { request =>
            complete {
              require(request.secret.length >= 10)

              val serviceX = request.secret.getBytes(Charsets.UTF_8)

              implicit val p: ExchangeParams = buildTestExchangeParams(
                Sha256Hash.hash(serviceX),
                10,
                Coin.valueOf(request.bitcoinAmount),
                request.startTimestampMillis.millis,
                // sync
                wavesNode.getHeight
              )

              val wavesUser = PrivateKeyAccount.fromPrivateKey(request.wavesUserWavesPrivateKey, p.wavesNetwork.toByte)
              val wavesTmpUser = PrivateKeyAccount.fromPrivateKey(request.wavesTmpPrivateKey, p.wavesNetwork.toByte)
              val wavesTmpPublicKey = new PublicKeyAccount(wavesTmpUser.getPublicKey, p.wavesNetwork.toByte)

              val bitcoinUserBitcoinECKey = ECKey.fromPrivate(request.bitcoinInputInfo.pk)
              val bitcoinUserWavesAccount = new PublicKeyAccount(request.bitcoinUserWavesPublicKey, p.wavesNetwork.toByte)


              val tx1 = WavesSide.sendMoneyToTempSwapAccount(wavesUser, wavesTmpUser, p.wavesFee)
              val tx1_1 = WavesSide.setSwapScriptOnTempSwapAccount(wavesNode, wavesTmpUser, wavesUser, bitcoinUserWavesAccount.getAddress, p.currentWavesHeight + 30, p.wavesFee)

              // TX2 - Bob Bitcoin -> scr2

              val wavesUserBitcoinECKey = ECKey.fromPrivate(KeysUtil.privateKeyBytesFromWIF(request.wavesUserBitcoinPrivateKeyWIF))

              val tx2 = BitcoinSide.createAtomicSwapTransaction(request.bitcoinInputInfo, bitcoinUserBitcoinECKey.getPubKey, wavesUserBitcoinECKey.getPubKey, p.startTimestamp.toSeconds + p.minutesTimeout.toSeconds)

              // TX3 - Service [normal case] - scr1 -> Bob Waves address
              // TX4 - Service [normal case] - scr1 -> Alice Bitcoin address

              val tx3 = ServiceSide.accomplishBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, serviceX, wavesUserBitcoinECKey.getPrivKeyBytes, wavesUserBitcoinECKey.getPubKey)
              val tx4 = ServiceSide.accomplishWavesSwapTransaction(wavesTmpPublicKey, bitcoinUserWavesAccount, serviceX, p.wavesSmartFee)

              // TX5 - Service (or someone) [failed case] - scr1 (TX0-1) -> Alice Waves address
              // TX6 - Service (or someone) [failed case] - scr2 (TX1) -> Bob Bitcoin address

              val tx5 = ServiceSide.recoverBitcoinSwapTransaction(tx2.id, tx2.underlying.getOutput(0).getScriptPubKey, p.bitcoinAmount.minus(p.bitcoinFee.multiply(2)), bitcoinUserBitcoinECKey.getPrivKeyBytes, bitcoinUserBitcoinECKey.getPubKey)
              val tx6 = ServiceSide.recoverWavesSwapTransaction(wavesTmpPublicKey, wavesUser, p.wavesSmartFee)

              Seq(tx1, tx1_1, tx2, tx3, tx4, tx5, tx6).view.map(_.stringify).toList.toJson
            }
          }
        }
      }
    }
  }
}

package com.wavesplatform.atomicswap

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import org.bitcoinj.core.Base58
import org.spongycastle.util.encoders.Hex

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object Api {
  private implicit val _system: ActorSystem = ActorSystem("api")
  private implicit val _materializer: ActorMaterializer = ActorMaterializer()
}
trait Api[SendTx <: BlockchainTransaction[_], ReadTx <: BlockchainTransaction[_], Block <: BlockchainBlock[ReadTx]] extends WithAwaitTransaction[ReadTx] {
  import Api._
  implicit protected val system: ActorSystem = _system
  implicit protected val materializer: ActorMaterializer = _materializer
  def sendTx(tx: SendTx, name: String): Future[Unit]
  def getBlock(height: Int): Future[Option[Block]]
  def getTransaction(txId: String): Future[Option[ReadTx]]
  def height: Future[Long]
}

trait WithAwaitTransaction[T <: BlockchainTransaction[_]] {
  self: Api[_, T, _] =>
  val confirmations: Int
  val tryEvery: FiniteDuration

  def waitForConfirmations(txId: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[Unit] = {
    Source.tick(0.seconds, 1 minute, ()).mapAsync(1)(_ => getTransaction(txId))
      .filter(tx => {
        tx.exists(_.confirmationsCount.exists(_ >= confirmations))
      }).completionTimeout(timeout.duration).runWith(Sink.head).map(_ => ())
  }
}

object ConsoleFakeNetwork extends Api[BlockchainTransaction[_], BlockchainTransaction[_], BlockchainBlock[BlockchainTransaction[_]]] with StrictLogging {
  override def sendTx(t: BlockchainTransaction[_], name: String): Future[Unit] = Future.successful {
    t match {
      case wt@WavesTransferTransaction(_, _) =>
        logger.info(s"Waves transaction $name tx id [${wt.id}]:")
        logger.info(Base58.encode(wt.bytes))
      case bt@BitcoinTransferTransaction(_, _) =>
        logger.info(s"Bitcoin transaction $name tx id [${bt.id}]:")
        logger.info(Hex.toHexString(bt.bytes))
      case _ =>
    }
  }
  override def getBlock(height: Int): Future[Option[BlockchainBlock[BlockchainTransaction[_]]]] = ???

  override def height: Future[Long] = ???
  override val confirmations: Int = ???
  override val tryEvery: FiniteDuration = ???
  override def getTransaction(txId: String): Future[Option[BlockchainTransaction[_]]] = ???
}

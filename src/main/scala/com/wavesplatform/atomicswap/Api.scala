package com.wavesplatform.atomicswap

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import org.bitcoinj.core.Base58
import org.spongycastle.util.encoders.Hex

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

object Api {
  private implicit val _system: ActorSystem = ActorSystem("api")
  private implicit val _materializer: ActorMaterializer = ActorMaterializer()
}

trait Api[SendTx <: BlockchainTransaction[_], ReadTx <: BlockchainTransaction[_], Block <: BlockchainBlock[ReadTx]] extends WithAwaitTransaction[SendTx, ReadTx] {

  import Api._

  implicit protected val system: ActorSystem = _system
  implicit protected val materializer: ActorMaterializer = _materializer

  def sendTx(tx: SendTx): Future[Unit]

  def getBlock(height: Int): Future[Option[Block]]

  def getTransaction(txId: String): Future[Option[ReadTx]]

  def height: Future[Long]

  def terminate(): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    _system.terminate().recoverWith { case NonFatal(_) => _system.whenTerminated }.map(_ => ())
  }
}

trait WithAwaitTransaction[S <: BlockchainTransaction[_], T <: BlockchainTransaction[_]] {
  self: Api[S, T, _] =>
  def confirmations: Int

  def tryEvery: FiniteDuration

  def waitForConfirmations(txId: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[Unit] = {
    Source.tick(0.seconds, 1 minute, ()).mapAsync(1)(_ => getTransaction(txId))
      .filter(tx => {
        tx.exists(_.confirmationsCount.exists(_ >= confirmations))
      }).completionTimeout(timeout.duration).runWith(Sink.head).map(_ => ())
  }

  def sendAndWaitForConfirmations(tx: S)(implicit timeout: Timeout, ec: ExecutionContext): Future[Unit] = {
    for {
      _ <- sendTx(tx)
      _ <- waitForConfirmations(tx.id)
    } yield ()
  }
}

object ConsoleFakeNetwork extends Api[BlockchainTransaction[_], BlockchainTransaction[_], BlockchainBlock[BlockchainTransaction[_]]] with StrictLogging {
  override def sendTx(t: BlockchainTransaction[_]): Future[Unit] = Future.successful {
    t match {
      case wt@WavesTransferTransaction(_, _) =>
        logger.info(s"Waves transaction - tx id [${wt.id}]:")
        logger.info(Base58.encode(wt.bytes))
      case bt@BitcoinTransferTransaction(_, _) =>
        logger.info(s"Bitcoin transaction - tx id [${bt.id}]:")
        logger.info(Hex.toHexString(bt.bytes))
      case _ =>
    }
  }

  override def getBlock(height: Int): Future[Option[BlockchainBlock[BlockchainTransaction[_]]]] = ???

  override def height: Future[Long] = ???

  override def confirmations: Int = ???

  override def tryEvery: FiniteDuration = ???

  override def getTransaction(txId: String): Future[Option[BlockchainTransaction[_]]] = ???
}

package com.wavesplatform.atomicswap.bitcoin

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.wavesplatform.atomicswap._
import com.wavesplatform.atomicswap.bitcoin.BitcoinApi.{BitcoinRpcBlock, BitcoinRpcTransaction}
import spray.json._
import wf.bitcoin.javabitcoindrpcclient.{BitcoinJSONRPCClient, BitcoinRPCException, BitcoindRpcClient}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object BitcoinApi extends SprayJsonSupport with DefaultJsonProtocol {

  case class BitcoinRpcTransaction(id: String, underlying: BitcoindRpcClient.RawTransaction) extends BlockchainTransaction[BitcoindRpcClient.RawTransaction] {
    override val bytes: Array[Byte] = Array.emptyByteArray

    override def bytesString: String = ""

    override def stringify: String = toString

    override val confirmation: Option[Int] = Try(underlying.confirmations()).toOption
  }

  case class BitcoinRpcBlock(hash: String, tx: Seq[BitcoinRpcTransaction], height: Int = -1) extends BlockchainBlock[BitcoinRpcTransaction] {
    override val transactions: Seq[BitcoinRpcTransaction] = tx

    override def stringify: String = toString
  }

}

class BitcoinApi(rpc: String, val confirmations: Int = 3, val tryEvery: FiniteDuration = 1 minute)(implicit ec: ExecutionContext) extends Api[BitcoinTransaction, BitcoinRpcTransaction, BitcoinRpcBlock] {

  import BitcoinApi._

  private val client = new BitcoinJSONRPCClient(rpc)

  override def sendTx(tx: BitcoinTransaction, name: String): Future[Unit] = Future {
    client.sendRawTransaction(tx.bytesString)
  }

  override def getBlock(height: Int): Future[Option[BitcoinRpcBlock]] = {
    Future {
      client.getBlock(height)
    }.map(Some.apply).recover { case e: BitcoinRPCException if e.getResponseCode == 500 => None }.flatMap {
      case Some(block) =>
        for {
          txs <- Future.sequence(block.tx.asScala.map(id => getTransaction(id)))
        } yield Some(BitcoinRpcBlock(block.hash(), txs.map(_.get), height))
      case None => Future.successful(None)
    }
  }

  override def getTransaction(txId: String): Future[Option[BitcoinRpcTransaction]] = {
    Future {
      client.getRawTransaction(txId)
    }.map(tx =>
      Some(BitcoinRpcTransaction(tx.txId(), tx))).recover { case e: BitcoinRPCException if e.getResponseCode == 500 => None }
  }

  override def height: Future[Long] = Future {
    client.getBlockCount
  }
}

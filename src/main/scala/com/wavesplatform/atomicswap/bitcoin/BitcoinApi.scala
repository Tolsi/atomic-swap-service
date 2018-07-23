package com.wavesplatform.atomicswap.bitcoin

import com.wavesplatform.atomicswap._
import wf.bitcoin.javabitcoindrpcclient.{BitcoinJSONRPCClient, BitcoinRPCException}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


class BitcoinApi(rpc: String, val confirmations: Int = 3, val tryEvery: FiniteDuration = 1 minute)(implicit ec: ExecutionContext) extends Api[BitcoinTransferTransaction, BitcoinRpcTransaction, BitcoinRpcBlock] {

  private val client = new BitcoinJSONRPCClient(rpc)

  override def sendTx(tx: BitcoinTransferTransaction, name: String): Future[Unit] = Future {
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

package com.wavesplatform.atomicswap.waves

import java.io.IOException

import com.wavesplatform.atomicswap.{Api, WavesBlock, WavesRpcTransferTransaction, WavesTransferTransaction}
import com.wavesplatform.wavesj.Node

import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

class WavesApi(node: String, val confirmations: Int = 10, val tryEvery: FiniteDuration = 1 minute) extends Api[WavesTransferTransaction, WavesRpcTransferTransaction, WavesBlock] {
  private val n = new Node(node)

  override def sendTx(tx: WavesTransferTransaction, name: String): Future[Unit] = Future {
    n.send(tx.underlying)
  }

  override def getBlock(height: Int): Future[Option[WavesBlock]] = Future {
    val b = n.getBlock(height)
    val txs = b.transactions.asScala.filter(tx => tx.get("type").asInstanceOf[Int] == 4).map(tx => WavesRpcTransferTransaction(WavesTransferTransactionV2(tx.asScala)))
    Some(WavesBlock(b.height, txs))
  }.recover {
    case _: IOException => None
  }

  override def getTransaction(txId: String): Future[Option[WavesRpcTransferTransaction]] = {
    val txF = Future {
      val js = n.getTransaction(txId).asScala
      js("type").asInstanceOf[Int] match {
        case 4 => Some(WavesRpcTransferTransaction(WavesTransferTransactionV2(js)))
        case _ => None
      }
    }.recover {
      case _: IOException => None
    }
    val heightF = height
    for {
      tx <- txF
      height <- heightF
    } yield {
      tx.map(tx => tx.copy(confirmationsCount = Some((height - tx.underlying.height).toInt)))
    }
  }

  override def height: Future[Long] = Future(n.getHeight.toLong)
}

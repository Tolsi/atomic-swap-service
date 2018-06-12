package com.wavesplatform.atomicswap

import com.typesafe.scalalogging.StrictLogging
import org.bitcoinj.core.Base58
import org.spongycastle.util.encoders.Hex

import scala.concurrent.Future

trait Network[T <: BlockchainTransaction[_]] {
  def sendTx(tx: T, name: String): Future[Unit]
}

object ConsoleFakeNetwork extends Network[BlockchainTransaction[_]] with StrictLogging {
  override def sendTx(t: BlockchainTransaction[_], name: String): Future[Unit] = Future.successful {
    t match {
      case wt@WavesTransaction(_) =>
        logger.info(s"Waves transaction $name tx id [${wt.id}]:")
        logger.info(Base58.encode(wt.bytes))
      case bt@BitcoinTransaction(_) =>
        logger.info(s"Bitcoin transaction $name tx id [${bt.id}]:")
        logger.info(Hex.toHexString(bt.bytes))
    }
  }
}

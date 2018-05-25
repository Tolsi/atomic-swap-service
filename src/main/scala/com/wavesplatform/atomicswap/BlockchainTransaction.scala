package com.wavesplatform.atomicswap

import com.wavesplatform.wavesj
import org.bitcoinj

sealed trait BlockchainTransaction[T] {
  val tx: T
}

case class WavesTransaction(override val tx: wavesj.Transaction) extends BlockchainTransaction[wavesj.Transaction]
case class BitcoinTransaction(override val tx: bitcoinj.core.Transaction) extends BlockchainTransaction[bitcoinj.core.Transaction]

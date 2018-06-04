package com.wavesplatform.atomicswap

import com.wavesplatform.wavesj
import com.wavesplatform.wavesj.Base58
import org.bitcoinj
import org.spongycastle.util.encoders.Hex

sealed trait BlockchainTransaction[T] {
  val underlying: T
  val id: String
  val bytes: Array[Byte]
  def bytesString: String
}

case class WavesTransaction(override val underlying: wavesj.Transaction) extends BlockchainTransaction[wavesj.Transaction] {
  override val id: String = underlying.id
  override val bytes: Array[Byte] = underlying.getBytes
  override val bytesString: String = Base58.encode(bytes)
}

case class BitcoinTransaction(override val underlying: bitcoinj.core.Transaction) extends BlockchainTransaction[bitcoinj.core.Transaction] {
  override val id: String = underlying.getHashAsString
  override val bytes: Array[Byte] = underlying.unsafeBitcoinSerialize
  override def bytesString: String = Hex.toHexString(bytes)
}

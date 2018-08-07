package com.wavesplatform.atomicswap

import com.wavesplatform.atomicswap.waves.WavesTransferTransactionV2
import com.wavesplatform.wavesj
import com.wavesplatform.wavesj.Base58
import org.bitcoinj
import org.spongycastle.util.encoders.Hex
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient

import scala.util.Try

sealed trait BlockchainTransaction[T] {
  val underlying: T

  def id: String

  val confirmationsCount: Option[Int]

  def bytes: Array[Byte]

  def bytesString: String

  def stringify: String
}

case class WavesTransferTransaction(override val underlying: wavesj.Transaction, override val confirmationsCount: Option[Int] = None) extends BlockchainTransaction[wavesj.Transaction] {
  override val id: String = underlying.id
  override val bytes: Array[Byte] = underlying.getBytes
  override val bytesString: String = Base58.encode(bytes)

  override def stringify: String = underlying.getJson
}

case class WavesRpcTransferTransaction(override val underlying: WavesTransferTransactionV2, override val confirmationsCount: Option[Int] = None) extends BlockchainTransaction[WavesTransferTransactionV2] {
  override def id: String = underlying.id

  override def bytes: Array[Byte] = ???

  override def bytesString: String = ???

  override def stringify: String = toString
}

case class BitcoinTransferTransaction(override val underlying: bitcoinj.core.Transaction, override val confirmationsCount: Option[Int] = None) extends BlockchainTransaction[bitcoinj.core.Transaction] {
  override val id: String = underlying.getHashAsString
  override val bytes: Array[Byte] = underlying.unsafeBitcoinSerialize

  override def bytesString: String = Hex.toHexString(bytes)

  override def stringify: String = bytesString
}

case class BitcoinRpcTransaction(id: String, underlying: BitcoindRpcClient.RawTransaction) extends BlockchainTransaction[BitcoindRpcClient.RawTransaction] {
  override def bytes: Array[Byte] = ???

  override def bytesString: String = ???

  override def stringify: String = toString

  override val confirmationsCount: Option[Int] = Try(underlying.confirmations()).toOption
}
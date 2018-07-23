package com.wavesplatform.atomicswap


trait BlockchainBlock[T <: BlockchainTransaction[_]] {
  val height: Int
  val transactions: Seq[T]
  def stringify: String
}

case class WavesBlock(height: Int, transactions: Seq[WavesRpcTransferTransaction]) extends BlockchainBlock[WavesRpcTransferTransaction] {
  override def stringify: String = toString
}

case class BitcoinRpcBlock(hash: String, tx: Seq[BitcoinRpcTransaction], height: Int = -1) extends BlockchainBlock[BitcoinRpcTransaction] {
  override val transactions: Seq[BitcoinRpcTransaction] = tx
  override def stringify: String = toString
}
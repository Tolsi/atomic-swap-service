package com.wavesplatform.atomicswap

trait BlockchainBlock[T <: BlockchainTransaction[_]] {
  val height: Int
  val transactions: Seq[T]
  def stringify: String
}

case class WavesBlock(height: Int, transactions: Seq[WavesTransaction]) extends BlockchainBlock[WavesTransaction] {
  override def stringify: String = toString
}
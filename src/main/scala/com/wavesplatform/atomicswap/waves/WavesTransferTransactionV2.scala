package com.wavesplatform.atomicswap.waves

import scala.collection.JavaConverters._

object WavesTransferTransactionV2 {
  def apply(dataS: collection.Map[String, AnyRef]): WavesTransferTransactionV2 = {
    val amount: Integer = dataS("amount").asInstanceOf[Int]
    val assetId: Option[String] = dataS.get("assetId").map(_.asInstanceOf[String])
    val attachment: String = dataS("attachment").asInstanceOf[String]
    val fee: Long = dataS("fee").asInstanceOf[Int]
    val feeAsset: Option[String] = dataS.get("feeAsset").map(_.asInstanceOf[String])
    val feeAssetId: Option[String] = dataS.get("feeAssetId").map(_.asInstanceOf[String])
    val height: Long = dataS("height").asInstanceOf[Int]
    val id: String = dataS("id").asInstanceOf[String]
    val proofs: List[String] = dataS.get("proofs").map(_.asInstanceOf[java.util.List[String]].asScala.toList).orElse(dataS.get("signature").map(s => List(s.asInstanceOf[String]))).get
    val recipient: String = dataS("recipient").asInstanceOf[String]
    val sender: String = dataS("sender").asInstanceOf[String]
    val senderPublicKey: String = dataS("senderPublicKey").asInstanceOf[String]
    val timestamp: Long = dataS("timestamp").asInstanceOf[Long]
    val `type`: Byte = dataS("type").asInstanceOf[Int].toByte
    val version: Byte = dataS("version").asInstanceOf[Int].toByte

    WavesTransferTransactionV2(amount.longValue(), assetId, attachment, fee, feeAsset, feeAssetId, height, id, proofs, recipient, sender, senderPublicKey, timestamp, `type`, version)
  }
}

case class WavesTransferTransactionV2(amount: Long, assetId: Option[String], attachment: String, fee: Long, feeAsset: Option[String],
                                      feeAssetId: Option[String], height: Long, id: String, proofs: List[String], recipient: String,
                                      sender: String, senderPublicKey: String, timestamp: Long, `type`: Byte, version: Byte)
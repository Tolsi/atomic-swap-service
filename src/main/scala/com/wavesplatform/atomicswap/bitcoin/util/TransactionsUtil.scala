package com.wavesplatform.atomicswap.bitcoin.util

import com.wavesplatform.atomicswap.ExchangeParams
import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import org.bitcoinj.core._
import org.bitcoinj.core.Transaction.SigHash
import org.bitcoinj.script.{Script, ScriptBuilder}
import org.bitcoinj.script.ScriptOpCodes._

import scala.collection.JavaConverters._

object TransactionsUtil {
  def createXHashUntilTimelockOrToSelfScript(digest: Array[Byte], oppositePublicKey: Array[Byte], timeout: Long, myPublicKey: Array[Byte]): Script = {
    new ScriptBuilder().op(OP_DEPTH).op(OP_2).op(OP_EQUAL).op(OP_IF)
      .op(OP_HASH256).data(digest).op(OP_EQUALVERIFY).data(oppositePublicKey).op(OP_CHECKSIG)
      .op(OP_ELSE).number(timeout).op(OP_CHECKLOCKTIMEVERIFY).op(OP_DROP).data(myPublicKey).op(OP_CHECKSIG)
      .op(OP_ENDIF)
      .build
  }

  def sendMoneyToScript(txInput: BitcoinInputInfo, v: Coin, outputScript: Script)(implicit p: ExchangeParams): Transaction = {
    val tx = new Transaction(p.networkParams)
    tx.setPurpose(Transaction.Purpose.USER_PAYMENT)
    tx.addOutput(v, outputScript)

    val input = tx.addInput(Sha256Hash.wrap(txInput.txId), txInput.outputIndex, txInput.script)
    val pk = ECKey.fromPrivate(txInput.pk)
    input.setScriptSig(ScriptBuilder.createInputScript(tx.calculateSignature(0, pk, txInput.script, SigHash.ALL, false), pk))
    tx
  }

  def createBackoutTransactionByTimeout(txInput: BitcoinInputInfo, v: Coin, outputScript: Script)(implicit p: ExchangeParams): Transaction = {
    val tx = new Transaction(p.networkParams)
    tx.setPurpose(Transaction.Purpose.USER_PAYMENT)
    tx.addOutput(v, outputScript)

    val input = tx.addInput(Sha256Hash.wrap(txInput.txId), txInput.outputIndex, txInput.script)
    // due https://github.com/bitcoin/bips/blob/master/bip-0065.mediawiki#detailed-specification
    // if (txTo.vin[nIn].IsFinal()) return false;
    input.setSequenceNumber(0)
    val pk = ECKey.fromPrivate(txInput.pk)
    val signature = tx.calculateSignature(0, pk, input.getScriptSig, SigHash.ALL, false)
    input.setScriptSig(new ScriptBuilder().data(signature.encodeToBitcoin()).build())
    tx
  }

  def createBackoutTransactionBySecret(txInput: BitcoinInputInfo, v: Coin, x: Array[Byte], outputScript: Script)(implicit p: ExchangeParams): Transaction = {
    val tx = new Transaction(p.networkParams)
    tx.setPurpose(Transaction.Purpose.USER_PAYMENT)
    tx.addOutput(v, outputScript)

    val input = tx.addInput(Sha256Hash.wrap(txInput.txId), txInput.outputIndex, txInput.script)
    val pk = ECKey.fromPrivate(txInput.pk)
    val signature = tx.calculateSignature(0, pk, input.getScriptSig, SigHash.ALL, false)
    input.setScriptSig(new ScriptBuilder().data(signature.encodeToBitcoin()).data(x).build())
    tx
  }
}

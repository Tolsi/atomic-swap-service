package com.wavesplatform.atomicswap.waves

import java.util.Base64

import com.wavesplatform.wavesj.{Base58, Node, PublicKeyAccount}

object AtomicSwapScriptSetScriptTransactionBuilder {
  def build(exchangePartyAddress: String,
            exchangeInitiatorAddress: String,
            exchangeInitiatorPubKey: PublicKeyAccount,
            secretHash: Array[Byte],
            timeoutHeight: Long): String = {
    s"""
    let Bob = extract(addressFromString("$exchangePartyAddress")).bytes
    let Alice = extract(addressFromString("$exchangeInitiatorAddress")).bytes
    let AlicesPK = base58'${Base58.encode(exchangeInitiatorPubKey.getPublicKey)}'
    match tx {
      case ttx: TransferTransaction =>
        let txRecipient = addressFromRecipient(ttx.recipient).bytes
        let txSender = addressFromPublicKey(ttx.senderPk).bytes

        let txToBob = (txRecipient == Bob) && (sha256(ttx.proofs[0]) == base58'${Base58.encode(secretHash)}') && ($timeoutHeight > height)
        let backToAliceAfterHeight = ((height >= $timeoutHeight) && (txRecipient == Alice))

        txToBob || backToAliceAfterHeight
      case other => false
    }"""
  }

  def compile(node: Node, script: String): Array[Byte] = {
    Base64.getDecoder.decode(node.compileScript(script).drop(7))
  }
}

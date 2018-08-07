package com.wavesplatform.atomicswap.waves

import java.util.Base64

import com.wavesplatform.wavesj.{Base58, Node, PublicKeyAccount}

object AtomicSwapScriptBuilder {
  def build(exchangePartyAddress: String,
            exchangeInitiatorAddress: String,
            exchangeInitiatorPubKey: PublicKeyAccount,
            secretHash: Array[Byte],
            timeoutHeight: Long): String = {
    s"""
    let Bob = Address(base58'$exchangePartyAddress')
    let Alice = Address(base58'$exchangeInitiatorAddress')

    match tx {
      case ttx: TransferTransaction =>
        let txToBob = (ttx.recipient == Bob) && (sha256(ttx.proofs[0]) == base58'${Base58.encode(secretHash)}') && ($timeoutHeight > height)
        let backToAliceAfterHeight = ((height >= $timeoutHeight) && (ttx.recipient == Alice))

        txToBob || backToAliceAfterHeight
      case other => false
    }"""
  }

  def compile(node: Node, script: String): Array[Byte] = {
    Base64.getDecoder.decode(node.compileScript(script).drop(7))
  }
}

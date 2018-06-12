package com.wavesplatform.atomicswap.waves

import com.wavesplatform.atomicswap.{ExchangeParams, WavesTransaction}
import com.wavesplatform.wavesj._

object WavesSide {
  def sendMoneyToTempSwapAccount(wavesUser: PrivateKeyAccount,
                                 wavesUserTmpPrivateKey: PublicKeyAccount,
                                 fee: Long
                               )(implicit p: ExchangeParams): WavesTransaction = {
    WavesTransaction(Transaction.makeTransferTx(wavesUser, wavesUserTmpPrivateKey.getAddress, p.wavesAmount + 100000 + 500000, Asset.WAVES, fee, Asset.WAVES, "", p.startTimestamp.toMillis))
  }

  def setSwapScriptOnTempSwapAccount(node: Node,
                                     wavesUserTmpPrivateKey: PrivateKeyAccount,
                                     wavesUser: PublicKeyAccount,
                                     bitcoinUserWavesAddress: String,
                                     timeoutHeight: Int,
                                     fee: Long
                                    )(implicit p: ExchangeParams): WavesTransaction = {
    val wavesSwapScript = AtomicSwapScriptSetScriptTransactionBuilder.build(bitcoinUserWavesAddress, wavesUser.getAddress, wavesUser,
      p.hashX, timeoutHeight)
    val wavesSwapScriptCompiled = AtomicSwapScriptSetScriptTransactionBuilder.compile(node, wavesSwapScript)
    WavesTransaction(Transaction.makeScriptTx(wavesUserTmpPrivateKey, Base64.encode(wavesSwapScriptCompiled), 'T', fee, p.startTimestamp.toMillis + 1))
  }
}

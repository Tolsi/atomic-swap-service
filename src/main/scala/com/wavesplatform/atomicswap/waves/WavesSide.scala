package com.wavesplatform.atomicswap.waves

import com.wavesplatform.atomicswap.{ExchangeParams, WavesTransferTransaction}
import com.wavesplatform.wavesj._

object WavesSide {
  def sendMoneyToTempSwapAccount(wavesUser: PrivateKeyAccount,
                                 wavesUserTmpPrivateKey: PublicKeyAccount,
                                 fee: Long
                                )(implicit p: ExchangeParams): WavesTransferTransaction = {
    WavesTransferTransaction(Transaction.makeTransferTx(wavesUser, wavesUserTmpPrivateKey.getAddress, p.wavesAmount + p.wavesFee + p.wavesSmartFee, Asset.WAVES, fee, Asset.WAVES, "", p.startTimestamp.toMillis))
  }

  def setSwapScriptOnTempSwapAccount(node: Node,
                                     wavesUserTmpPrivateKey: PrivateKeyAccount,
                                     wavesUser: PublicKeyAccount,
                                     bitcoinUserWavesAddress: String,
                                     timeoutHeight: Int,
                                     fee: Long
                                    )(implicit p: ExchangeParams): WavesTransferTransaction = {
    val wavesSwapScript = AtomicSwapScriptBuilder.build(bitcoinUserWavesAddress, wavesUser.getAddress, wavesUser,
      p.hashX, timeoutHeight)
    val wavesSwapScriptCompiled = AtomicSwapScriptBuilder.compile(node, wavesSwapScript)
    WavesTransferTransaction(Transaction.makeScriptTx(wavesUserTmpPrivateKey, Base64.encode(wavesSwapScriptCompiled), p.wavesNetwork.toByte, fee, p.startTimestamp.toMillis + 1))
  }
}

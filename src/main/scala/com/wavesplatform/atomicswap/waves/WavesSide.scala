package com.wavesplatform.atomicswap.waves

import com.wavesplatform.atomicswap.ExchangeParams
import com.wavesplatform.wavesj._

object WavesSide {
  def sendMoneyToTempSwapAccount(wavesUser: PrivateKeyAccount,
                                 wavesUserTmpPrivateKey: PublicKeyAccount,
                                 fee: Long
                               )(implicit p: ExchangeParams): Transaction = {
    Transaction.makeTransferTx(wavesUser, wavesUserTmpPrivateKey.getAddress, p.wavesAmount, Asset.WAVES, fee, Asset.WAVES, "")
  }

  def setSwapScriptOnTempSwapAccount(wavesUserTmpPrivateKey: PrivateKeyAccount,
                                     wavesUser: PublicKeyAccount,
                                     bitcoinUserWavesAddress: String,
                                     timeoutHeight: Int,
                                     fee: Long
                                    )(implicit p: ExchangeParams): Transaction = {
    val wavesSwapScript = AtomicSwapScriptSetScriptTransactionBuilder.build(bitcoinUserWavesAddress, wavesUser.getAddress, wavesUser,
      p.hashX, timeoutHeight, wavesUserTmpPrivateKey, fee, p.startTimestampSeconds)
    Transaction.makeScriptTx(wavesUserTmpPrivateKey, Base58.encode(wavesSwapScript), 'T', 100000)
  }
}

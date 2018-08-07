package com.wavesplatform.atomicswap.bitcoin.coinswap

import com.typesafe.scalalogging.StrictLogging
import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import com.wavesplatform.atomicswap.bitcoin.coinswap.util.TransactionsUtil.{createXHashUntilTimelockOrToSelfScript, sendMoneyToScript}
import com.wavesplatform.atomicswap.{BitcoinTransferTransaction, ExchangeParams}

object BitcoinSide extends StrictLogging {
  def createAtomicSwapTransaction(bitcoinUserBitcoinOutInfo: BitcoinInputInfo,
                                  bitcoinUserPubKey: Array[Byte],
                                  wavesUserBitcoinPublicKey: Array[Byte],
                                  timeoutTs: Long)(implicit p: ExchangeParams): BitcoinTransferTransaction = {
    val T2script = createXHashUntilTimelockOrToSelfScript(p.hashX, wavesUserBitcoinPublicKey, timeoutTs, bitcoinUserPubKey)
    BitcoinTransferTransaction(sendMoneyToScript(bitcoinUserBitcoinOutInfo, p.bitcoinAmount.minus(p.bitcoinFee), T2script))
  }
}

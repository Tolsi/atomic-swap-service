package com.wavesplatform.atomicswap.bitcoin.coinswap

import com.typesafe.scalalogging.StrictLogging
import com.wavesplatform.atomicswap.{BitcoinTransaction, ExchangeParams}
import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import com.wavesplatform.atomicswap.bitcoin.coinswap.util.TransactionsUtil.{createXHashUntilTimelockOrToSelfScript, sendMoneyToScript}

object BitcoinSide extends StrictLogging {
  def createAtomicSwapTransaction(bitcoinUserBitcoinOutInfo: BitcoinInputInfo,
                                  bitcoinUserPubKey: Array[Byte],
                                  wavesUserBitcoinPublicKey: Array[Byte],
                                  timeoutTs: Long)(implicit p: ExchangeParams): BitcoinTransaction = {
    val T2script = createXHashUntilTimelockOrToSelfScript(p.hashX, wavesUserBitcoinPublicKey, timeoutTs, bitcoinUserPubKey)
    BitcoinTransaction(sendMoneyToScript(bitcoinUserBitcoinOutInfo, p.bitcoinAmount.minus(p.fee), T2script))
  }
}

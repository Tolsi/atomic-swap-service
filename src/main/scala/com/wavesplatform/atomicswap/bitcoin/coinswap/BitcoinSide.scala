package com.wavesplatform.atomicswap.bitcoin.coinswap

import com.typesafe.scalalogging.StrictLogging
import com.wavesplatform.atomicswap.ExchangeParams
import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import com.wavesplatform.atomicswap.bitcoin.coinswap.util.TransactionsUtil.{createXHashUntilTimelockOrToSelfScript, sendMoneyToScript}
import org.bitcoinj.core.Transaction

object BitcoinSide extends StrictLogging {
  def createAtomicSwapTransaction(bitcoinUserBitcoinOutInfo: BitcoinInputInfo,
                                  bitcoinUserPubKey: Array[Byte],
                                  wavesUserBitcoinPublicKey: Array[Byte],
                                  timeoutTs: Long)(implicit p: ExchangeParams): Transaction = {
    val T2script = createXHashUntilTimelockOrToSelfScript(p.hashX, wavesUserBitcoinPublicKey, timeoutTs, bitcoinUserPubKey)
    sendMoneyToScript(bitcoinUserBitcoinOutInfo, p.bitcoinAmount, T2script)
  }
}

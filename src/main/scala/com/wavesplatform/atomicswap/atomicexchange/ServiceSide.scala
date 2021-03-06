package com.wavesplatform.atomicswap.atomicexchange

import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import com.wavesplatform.atomicswap.bitcoin.coinswap.util.TransactionsUtil.{createBackoutTransactionByTimeout, createBackoutTransactionByX}
import com.wavesplatform.atomicswap.{BitcoinTransferTransaction, ExchangeParams, WavesTransferTransaction}
import com.wavesplatform.wavesj
import com.wavesplatform.wavesj.{Asset, Base58, PublicKeyAccount, Transaction}
import org.bitcoinj.core.{Coin, ECKey}
import org.bitcoinj.script.{Script, ScriptBuilder}

object ServiceSide {
  def accomplishWavesSwapTransaction(
                                      serviceWavesPublicKey: PublicKeyAccount,
                                      wavesUser: PublicKeyAccount,
                                      serviceX: Array[Byte],
                                      fee: Long
                                    )(implicit p: ExchangeParams): WavesTransferTransaction = {
    val tx = wavesj.Transaction.makeTransferTx(serviceWavesPublicKey, wavesUser.getAddress, p.wavesAmount, Asset.WAVES, fee, Asset.WAVES, "", p.startTimestamp.toMillis + 2)
    WavesTransferTransaction(tx.withProof(0, Base58.encode(serviceX)))
  }


  def accomplishBitcoinSwapTransaction(tx2Id: String,
                                       T2script: Script,
                                       serviceX: Array[Byte],
                                       wavesUserBitcoinPrivateKey: Array[Byte],
                                       wavesUserBitcoinPublic: Array[Byte]
                                      )(implicit p: ExchangeParams): BitcoinTransferTransaction = {
    val tx4amount = p.bitcoinAmount.minus(p.bitcoinFee.multiply(2))
    BitcoinTransferTransaction(createBackoutTransactionByX(BitcoinInputInfo(tx2Id, 0, T2script, wavesUserBitcoinPrivateKey), tx4amount, serviceX,
      ScriptBuilder.createOutputScript(ECKey.fromPublicOnly(wavesUserBitcoinPublic).toAddress(p.bitcoinNetworkParams))))
  }

  def recoverBitcoinSwapTransaction(tx2Id: String,
                                    T2script: Script,
                                    amount: Coin,
                                    serviceBitcoinPrivateKey: Array[Byte],
                                    bitcoinUserPubKey: Array[Byte])(implicit p: ExchangeParams): BitcoinTransferTransaction = {
    BitcoinTransferTransaction(createBackoutTransactionByTimeout(BitcoinInputInfo(tx2Id, 0, T2script, serviceBitcoinPrivateKey), amount,
      ScriptBuilder.createOutputScript(ECKey.fromPublicOnly(bitcoinUserPubKey).toAddress(p.bitcoinNetworkParams)), p.startTimestamp.toSeconds + p.minutesTimeout.toSeconds))
  }

  def recoverWavesSwapTransaction(wavesUserTmpPrivateKey: PublicKeyAccount,
                                  wavesUser: PublicKeyAccount,
                                  fee: Long)
                                 (implicit p: ExchangeParams): WavesTransferTransaction = {
    WavesTransferTransaction(Transaction.makeTransferTx(wavesUserTmpPrivateKey, wavesUser.getAddress, p.wavesAmount, Asset.WAVES, fee, Asset.WAVES, "", p.startTimestamp.toMillis + 2))
  }
}

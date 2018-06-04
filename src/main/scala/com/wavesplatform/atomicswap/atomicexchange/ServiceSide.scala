package com.wavesplatform.atomicswap.atomicexchange

import com.wavesplatform.atomicswap.{BitcoinTransaction, ExchangeParams, WavesTransaction}
import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import com.wavesplatform.atomicswap.bitcoin.coinswap.util.TransactionsUtil.{createBackoutTransactionByTimeout, createBackoutTransactionByX}
import com.wavesplatform.wavesj.{Asset, Base58, PublicKeyAccount, Transaction}
import com.wavesplatform.wavesj
import org.bitcoinj.core.{Coin, ECKey}
import org.bitcoinj.script.{Script, ScriptBuilder}

object ServiceSide {
  def accomplishWavesSwapTransaction(
                                      serviceWavesPublicKey: PublicKeyAccount,
                                      wavesUser: PublicKeyAccount,
                                      serviceX: Array[Byte],
                                      fee: Long
                                    )(implicit p: ExchangeParams): WavesTransaction = {
    val tx = wavesj.Transaction.makeTransferTx(serviceWavesPublicKey, wavesUser.getAddress, p.wavesAmount, Asset.WAVES, fee, Asset.WAVES, "", p.startTimestampMillis + 2)
    tx.setProof(0, Base58.encode(serviceX))
    WavesTransaction(tx)
  }


  def accomplishBitcoinSwapTransaction(tx2Id: String,
                                       T2script: Script,
                                       serviceX: Array[Byte],
                                       serviceBitcoinPrivateKey: Array[Byte],
                                       bitcoinUserPubKey: Array[Byte]
                                      )(implicit p: ExchangeParams): BitcoinTransaction = {
    val tx4amount = p.bitcoinAmount.minus(p.fee.multiply(1))
    BitcoinTransaction(createBackoutTransactionByX(BitcoinInputInfo(tx2Id, 0, T2script, serviceBitcoinPrivateKey), tx4amount, serviceX,
      ScriptBuilder.createOutputScript(ECKey.fromPublicOnly(bitcoinUserPubKey).toAddress(p.networkParams))))
  }

  def recoverBitcoinSwapTransaction(tx2Id: String,
                                    T2script: Script,
                                    amount: Coin,
                                    serviceBitcoinPrivateKey: Array[Byte],
                                    bitcoinUserPubKey: Array[Byte])(implicit p: ExchangeParams): BitcoinTransaction = {
    BitcoinTransaction(createBackoutTransactionByTimeout(BitcoinInputInfo(tx2Id, 0, T2script, serviceBitcoinPrivateKey), amount,
      ScriptBuilder.createOutputScript(ECKey.fromPublicOnly(bitcoinUserPubKey).toAddress(p.networkParams))))
  }

  def recoverWavesSwapTransaction(wavesUserTmpPrivateKey: PublicKeyAccount,
                                  wavesUser: PublicKeyAccount,
                                  fee: Long)
                                 (implicit p: ExchangeParams): WavesTransaction = {
    WavesTransaction(Transaction.makeTransferTx(wavesUserTmpPrivateKey, wavesUser.getAddress, p.wavesAmount, Asset.WAVES, fee, Asset.WAVES, "", p.startTimestampMillis + 2))
  }
}
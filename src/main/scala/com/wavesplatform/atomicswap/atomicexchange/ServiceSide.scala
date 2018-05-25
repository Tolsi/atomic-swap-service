package com.wavesplatform.atomicswap.atomicexchange

import com.wavesplatform.atomicswap.ExchangeParams
import com.wavesplatform.atomicswap.bitcoin.BitcoinInputInfo
import com.wavesplatform.atomicswap.bitcoin.coinswap.util.TransactionsUtil.{createBackoutTransactionByTimeout, createBackoutTransactionByX}
import com.wavesplatform.wavesj.{Asset, Base58, PublicKeyAccount, Transaction}
import com.wavesplatform.wavesj
import org.bitcoinj
import org.bitcoinj.core.{Coin, ECKey}
import org.bitcoinj.script.{Script, ScriptBuilder}

object ServiceSide {
  def accomplishWavesSwapTransaction(
                                      serviceWavesPublicKey: PublicKeyAccount,
                                      wavesUser: PublicKeyAccount,
                                      serviceX: Array[Byte],
                                      fee: Long
                                    )(implicit p: ExchangeParams): wavesj.Transaction = {
    val tx = wavesj.Transaction.makeTransferTx(serviceWavesPublicKey, wavesUser.getAddress, p.wavesAmount, Asset.WAVES, fee, Asset.WAVES, "")
    tx.setProof(0, Base58.encode(serviceX))
    tx
  }


  def accomplishBitcoinSwapTransaction(tx2Id: String,
                                       T2script: Script,
                                       serviceX: Array[Byte],
                                       serviceBitcoinPrivateKey: Array[Byte],
                                       bitcoinUserPubKey: Array[Byte]
                                      )(implicit p: ExchangeParams): bitcoinj.core.Transaction = {
    val tx4amount = p.bitcoinAmount.minus(p.fee.multiply(1))
    createBackoutTransactionByX(BitcoinInputInfo(tx2Id, 0, T2script, serviceBitcoinPrivateKey), tx4amount, serviceX,
      ScriptBuilder.createOutputScript(ECKey.fromPublicOnly(bitcoinUserPubKey).toAddress(p.networkParams)))
  }

  def recoverBitcoinSwapTransaction(tx2Id: String,
                                    T2script: Script,
                                    amount: Coin,
                                    serviceBitcoinPrivateKey: Array[Byte],
                                    bitcoinUserPubKey: Array[Byte])(implicit p: ExchangeParams): bitcoinj.core.Transaction = {
    createBackoutTransactionByTimeout(BitcoinInputInfo(tx2Id, 0, T2script, serviceBitcoinPrivateKey), amount,
      ScriptBuilder.createOutputScript(ECKey.fromPublicOnly(bitcoinUserPubKey).toAddress(p.networkParams)))
  }

  def recoverWavesSwapTransaction(serviceWavesPublicKey: PublicKeyAccount,
                                  wavesUser: PublicKeyAccount,
                                  fee: Long)
                                 (implicit p: ExchangeParams): wavesj.Transaction = {
    Transaction.makeTransferTx(serviceWavesPublicKey, wavesUser.getAddress, p.wavesAmount, Asset.WAVES, fee, Asset.WAVES, "")
  }
}

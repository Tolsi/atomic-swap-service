package com.wavesplatform.atomicswap


import org.bitcoinj.core.{Coin, NetworkParameters}

import scala.concurrent.duration.FiniteDuration

case class ExchangeParams(bitcoinNetworkParams: NetworkParameters,
                          wavesNetwork: Char,
                          bitcoinFee: Coin,
                          minutesTimeout: FiniteDuration,
                          wavesBlocksTimeout: Long,
                          wavesAmount: Int,
                          bitcoinAmount: Coin,
                          wavesFee: Long,
                          wavesSmartFee: Long,
                          hashX: Array[Byte],
                          startTimestamp: FiniteDuration,
                          network: Api[BlockchainTransaction[_], BlockchainTransaction[_], BlockchainBlock[BlockchainTransaction[_]]],
                          currentWavesHeight: Int)
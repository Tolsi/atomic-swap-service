package com.wavesplatform.atomicswap


import org.bitcoinj.core.{Coin, NetworkParameters}

import scala.concurrent.duration.FiniteDuration

case class ExchangeParams(networkParams: NetworkParameters,
                          fee: Coin,
                          timeout: FiniteDuration,
                          wavesAmount: Int,
                          bitcoinAmount: Coin,
                          hashX: Array[Byte],
                          startTimestamp: Long,
                          network: Network,
                          currentWavesHeight: Int)
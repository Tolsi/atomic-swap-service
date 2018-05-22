package com.wavesplatform.atomicswap.bitcoin

import org.bitcoinj.script.Script

case class BitcoinInputInfo(txId: String, outputIndex: Int, script: Script, private[atomicswap] val pk: Array[Byte])
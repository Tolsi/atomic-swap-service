package com.wavesplatform.atomicswap.bitcoin.util

import org.bitcoinj.core.Base58

object KeysUtil {
  def privateKeyBytesFromWIF(wif: String): Array[Byte] = {
    val wifBytes = Base58.decode(wif)
    val withoutMetaData = wifBytes.dropRight(4).drop(1)
    // is public key is compressed
    if (withoutMetaData.last == 1) {
      withoutMetaData.dropRight(1)
    } else withoutMetaData
  }
}

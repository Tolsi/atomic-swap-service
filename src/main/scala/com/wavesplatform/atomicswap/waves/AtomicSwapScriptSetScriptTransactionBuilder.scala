package com.wavesplatform.atomicswap.waves

import com.google.common.primitives.Longs
import com.wavesplatform.wavesj.{Base58, PublicKeyAccount}

object AtomicSwapScriptSetScriptTransactionBuilder {
  private val transactionTemplateBytes: Array[Byte] = Base58 .decode(
      "4mN7bdkm6qj9Tbg6WSeARNLH6B66GX2QAFHZp72Uh3kLorX9CAB8CECBHivzb8jUWzESE86GcPZQRb79xJTkBLv8ArgSREsMTRC5dDfZXya2V86zHP3ArcZL83QFaGjaLR1e63MVYCK9NE5YpBvzpWy1gf6JMA2Uz3drAqBTm1SivkHFPsHP6M9n6Zt1ZdsaQdpU82iSkL6GpDvU75wv6a61VCc6C6gFvXJZgmF3CrDJapK7N4XMUmVK5rwD6UaazeegpdbNHB47yKPJ76PE1dnahMXLfYyj68HUw5Yc3APbhiqrzY8ntWfaDGPGRjiqoh1mXBCQLS3Thna6koq7YZwDfmeE1NhTfthWYpRcX2AqvqJwxX1mSjzsBNY4xuC2q3kYjgxtbGKabsuWyjsAiDU5t1P4wAu1wq7BwkpoExEK3PCf7sCTvZGVw88nEwyZzNqDQ665QePU2AZkvCo1nzwgyHjzFEvNFnGvcdHi2xai82aLVG2keTj21vRgHVwyeyHWuhEGmwHReLsLSHFNUbi3NgaSZc3jHFDS3jLt1wJirX7TH1Vs1PfPMZHayQm6DGVxv17Ae9AFQxyMCxvVVMCFx1R7ZHWNN1QfR5kwSyK2FGNnXJ6bdWzQsp3uto7tJDa6LtuVt7CBt4twWGHC4CCsvVFrXzUVPrumT9N6KLWV1uTzyUFLdKZnnzEWSmgAjCGKXvGHxfeDx7dfhSdKdkQfauJxfYTXz9RGwE6gYs5QmyQr9D83jH1UqEY7s7hhuzyPXEvZhDKyXfmGLPBoDQuFZDyUK998SoZKWQkXR8MKGGzPJYGQN6junxPjf8cVVuTZNf9JRLJiA79kbeWToDcsctaaitaG6yXJRiFSUuxcqTci31pgG9GocNa5kQz3LobHQf6w89jdt9mxpjfrwz1CQ2u6rpE1ZJFbSRvp4Mh2r9qKu8ANae4xcuSyjGvQKecWbHeyk9jq8PKG8vRcAWxJrRQnYaPgsprs4TbUEhFQLywcTWrs7fYme1MB4wZ51gKsrGqVKsae6jjd2EJmb4cYGSUgUwNqo9MvtmUsaP4ui47JroC7GNPfvUZdNFC32JZwadfX8weS7xyKmS6tTZ3yCNd9Wd93DB3nF7HtvkPntgSm6PXzpA4GF97o4LXaK1XBmGotzuv68w77i3PPzv421ChiryDXXu2yqUt7AGyqfhsxZVbJxcqC5tnGkxNbFjPSLpRJgfSJu2VG7rFzFpgSJ75R8sutFVP8z3NcxNnLBHGyzCiM2cKGk6KMPXDqUwFWJREkue6Pj56oacHWKyoUe2XAtHQkwoYZqqn68BbkDVRzkN3qj1WoEmT2bNcAaLbhGCvnKAsNFJeJ66hPkSWXnkRpeJzSAeCbFJ8YvyobzBZ9At1Sggk3L131FhJSaumQLMD8Gvho65E9AbMhdg1F6P7bVWk9Lz92SFETD6D6f")

  def build(exchangePartyAddress: String,
            exchangeInitiatorAddress: String,
            exchangeInitiatorPubKey: PublicKeyAccount,
            secretHash: Array[Byte],
            timeoutHeight: Long,
            txSenderPubKey: PublicKeyAccount,
            fee: Long,
            timestamp: Long): Array[Byte] = {
    var localResult = transactionTemplateBytes.clone()
    localResult = localResult.patch(3, txSenderPubKey.getPublicKey, 32)
    localResult = localResult.patch(107, exchangePartyAddress.getBytes, 35)
    localResult = localResult.patch(247, exchangeInitiatorAddress.getBytes, 35)
    localResult = localResult.patch(339, exchangeInitiatorPubKey.getPublicKey, 32)
    localResult = localResult.patch(803, secretHash, 32)
    localResult = localResult.patch(855, Longs.toByteArray(timeoutHeight), 8)
    localResult = localResult.patch(936, Longs.toByteArray(timeoutHeight), 8)
    localResult = localResult.patch(1048, Longs.toByteArray(fee), 8)
    localResult = localResult.patch(1056, Longs.toByteArray(timestamp), 8)
    localResult
  }
}

# Atomic swap service [WIP]

This project is designed to implement an atomic-swap service between Waves and other crypto-currencies.

The classical scheme of atomic exchange on the blockchain requires both participants to monitor the blocks on the both chains, but this implementation shifts all the "waiting and follow-up" operations to an external service. Thus, it is required to give confidence to the service for sending operations to the blockchain, but at the same time the money never gets to accounts of exchange, that is, they remain under the control of the participants in the user exchange. In the worst case, users will be able to recover their money and exchange will not take place, but no one will be deceived.

# Demo

At the current stage, tools are being designed to fully implement atomic exchange between chains, which requires, for example, access to signature tools on integrable networks and wallets with private keys. This requires new solutions for the user experience.

You can download and run the server demo from the release section, which is the API for creating transactions for atomic exchange between Waves and Bitcoin testnets. You can send the generated transactions to the target networks (for example using [nodes](https://testnet.wavesexplorer.com/nodes) or [explorers](https://live.blockcypher.com/btc-testnet/)) and check that they work as stated. Now it is still being tested ().

You can start the demo using

```
java -jar atomic-swap-service-assembly-0.0.1.jar
```

You need Java 8 installed. After that you can call a requests to it

```
curl -X POST \
  http://localhost:8080/exchange/demo \
  -H 'content-type: application/json' \
  -d '{
	"secret": "supersecretstring",
	"wavesAmount": 10000,
    "bitcoinAmount": 8125000,
    "wavesUserWavesPrivateKey": "5uWWqbS8gV2KQXPjTuh4RdDLS5f2ADbRghVXBSboTEEz",
    "wavesTmpPrivateKey": "82Dag3ZGapnT1XfBbMd9kHyrmLsQsfsGozgLsCUf2ci2",
    "wavesUserBitcoinPrivateKeyWIF": "91jVRNUJWu9ousWk6LdeUFRPcdFDmBiw5jnpYLogE2Ki8AwiZQg",
    "bitcoinUserWavesPublicKey": "9BFWWHXXZgp2b19pFEFH1eqnetEr3qJX2zie6kBgSTun",
    "bitcoinInputInfo": {
	    "txId":"034f4edae5fd0c91b51fa09ccef4eb8b3ce0eccd15bed99201c4e9ef41533480",
	    "outputIndex":0,
	    "bitcoinUserPrivateKeyWIF": "91h8oWwuCkzxs979qFNXLF9raNxewMYozW2MnT9pPJ8mids26Wi"
    }
}'

200

[
    "{\"senderPublicKey\":\"Cx7w4nv3gGBPdxjjPLm8qGk4zccFQzeLpv2TAgcMRweu\",\"amount\":600010,\"attachment\":\"\",\"signature\":\"5tPcyuPAfrzxoY83zDwuV4kzQsvZQaM1AezgDsD9SLHGKh8iWF5ETRRiQ7Rxaaz3NRKgZSCfEJDG6b6h7zyijGGb\",\"proofs\":[\"5tPcyuPAfrzxoY83zDwuV4kzQsvZQaM1AezgDsD9SLHGKh8iWF5ETRRiQ7Rxaaz3NRKgZSCfEJDG6b6h7zyijGGb\"],\"fee\":100000,\"recipient\":\"3Mtio6bzcaXNzK4ASUAtxDT6ZXAmBA7SrFp\",\"id\":\"EAHand4XcWm5u5BjxNm72VpUk21ztXzQ5DrpopRk3ybB\",\"type\":4,\"version\":2,\"timestamp\":1528836856673}",
    "{\"senderPublicKey\":\"AjBes1UuGzYyjcbGE4shdG8E6UkeCBg7ZF2kVi3NvxiW\",\"signature\":\"2voQ3tdyxnTcTK7KHuCQKg1C8ngUZqAucAGMT68d73VeXCLHw9aAd33zBcMdqdDwTPnjEHQ15vFGY5Gq7ycaRZDW\",\"proofs\":[\"2voQ3tdyxnTcTK7KHuCQKg1C8ngUZqAucAGMT68d73VeXCLHw9aAd33zBcMdqdDwTPnjEHQ15vFGY5Gq7ycaRZDW\"],\"fee\":100000,\"id\":\"9MfjhgQmrYKKYxsANUCAygmhBmNKDkQrm9p4HnBR58hw\",\"type\":13,\"version\":1,\"script\":\"base64:AQQAAAADQm9iCAkAAAAHZXh0cmFjdAAAAAEJAAAAEWFkZHJlc3NGcm9tU3RyaW5nAAAAAQIAAAAjM010Z0FvUHZrWmZzZjlwcWtwcWtNN3dHem01ZzJSdTJVY2YAAAAFYnl0ZXMEAAAABUFsaWNlCAkAAAAHZXh0cmFjdAAAAAEJAAAAEWFkZHJlc3NGcm9tU3RyaW5nAAAAAQIAAAAjM04yQms5RXRxVzVQR3VjMzhwWWFFVUt6cXNNakV5ZHdYRmkAAAAFYnl0ZXMEAAAACEFsaWNlc1BLAQAAAAAAAAAgsY9Wwr9xigNHBUO2rbRrYSxVTR25BFWbGPaYBeDEHk4EAAAAByRtYXRjaDAFAAAAAnR4AwkAAAAEX2lpbwAAAAIFAAAAByRtYXRjaDACAAAAE1RyYW5zZmVyVHJhbnNhY3Rpb24EAAAAA3R0eAUAAAAHJG1hdGNoMAQAAAALdHhSZWNpcGllbnQICQAAABRhZGRyZXNzRnJvbVJlY2lwaWVudAAAAAEIBQAAAAN0dHgAAAAJcmVjaXBpZW50AAAABWJ5dGVzBAAAAAh0eFNlbmRlcggJAAAAFGFkZHJlc3NGcm9tUHVibGljS2V5AAAAAQgFAAAAA3R0eAAAAAhzZW5kZXJQawAAAAVieXRlcwQAAAAHdHhUb0JvYgMDCQAAAAN2PXYAAAACBQAAAAt0eFJlY2lwaWVudAUAAAADQm9iCQAAAAN2PXYAAAACCQAAAAZzaGEyNTYAAAABCQAAAApnZXRFbGVtZW50AAAAAggFAAAAA3R0eAAAAAZwcm9vZnMAAAAAAAAAAAABAAAAAAAAACCCnSvyMSfGnkbqDjTU2hLFFZ+Q3Fthj5fYrwE+jmVBTwcJAAAAA2w+bAAAAAIAAAAAAAAFwj4FAAAABmhlaWdodAcEAAAAFmJhY2tUb0FsaWNlQWZ0ZXJIZWlnaHQDCQAAAARiPj1iAAAAAgUAAAAGaGVpZ2h0AAAAAAAABcI+CQAAAAN2PXYAAAACBQAAAAt0eFJlY2lwaWVudAUAAAAFQWxpY2UHAwUAAAAHdHhUb0JvYgYFAAAAFmJhY2tUb0FsaWNlQWZ0ZXJIZWlnaHQEAAAABW90aGVyBQAAAAckbWF0Y2gwBy66nzQ=\",\"timestamp\":1528836856674}",
    "0100000001adb0b04bd1e9405c1b23f184ed7d448766906e5dfc0b28ee9ff55241cfebc0e3000000006b483045022100eb5ff528acadfc329a0a7d3233b280792c7ef4cbc50bfb466321456d754ee77d022026d820c0e3cd8e2e0f853d5b3ed5afb1d06ad836f175f40fdf2bffe7448b772f012102a3ea55e461f15bc98aad469ae8d70224072e131435133ba8a5604d3d0a6a3b5effffffff0188334e00000000007674528763a820829d2bf23127c69e46ea0e34d4da12c5159f90dc5b618f97d8af013e8e65414f882103c6d6f3806339ddae524f467bca4cbe401bc8ca16449ab4ba4f66a46454cc8124ac6704003a205bb1752102a3ea55e461f15bc98aad469ae8d70224072e131435133ba8a5604d3d0a6a3b5eac6800000000",
    "0100000001905e986367dbbbdbd294b12dad54374671d09cffae4c5f912b9458f734ef280e000000005b483045022100a7851d3bff67bfa4145edbef865f3eedb2c94cea1b7208328b9e9e4aa174a7e80220028fbffe26100794eb2090a3ec053d2055a7193e70c0ef53d5e9c242e720ac1801117375706572736563726574737472696e67ffffffff0148f13e00000000001976a9147e51e6c7ac2f9edc4627e68e0ac04bf6b82c534788ac00000000",
    "{\"senderPublicKey\":\"AjBes1UuGzYyjcbGE4shdG8E6UkeCBg7ZF2kVi3NvxiW\",\"amount\":10,\"attachment\":\"\",\"signature\":\"25vsxZEhrfb9gsGYusxPqWRc\",\"proofs\":[\"25vsxZEhrfb9gsGYusxPqWRc\"],\"fee\":500000,\"recipient\":\"3MtgAoPvkZfsf9pqkpqkM7wGzm5g2Ru2Ucf\",\"id\":\"4yHshz64yKQs6dyJHWbBTXWzs71gCg3AKVwCVFmA1s7H\",\"type\":4,\"version\":2,\"timestamp\":1528836856675}",
    "0100000001905e986367dbbbdbd294b12dad54374671d09cffae4c5f912b9458f734ef280e0000000049483045022100f4920e1ce2106724855546b640023578ac64669153ef873c7f079c760628cd420220468a00875c2caea64017c6cccb918dcbc662fc7e5c2e9a51d4eb363a10aa1e6301000000000148f13e00000000001976a914bf5ac26af2ea09ba0c8873377d14397118d9463788ac09070000",
    "{\"senderPublicKey\":\"AjBes1UuGzYyjcbGE4shdG8E6UkeCBg7ZF2kVi3NvxiW\",\"amount\":10,\"attachment\":\"\",\"proofs\":[],\"fee\":500000,\"recipient\":\"3N2Bk9EtqW5PGuc38pYaEUKzqsMjEydwXFi\",\"id\":\"6KaKPv35XrLK6Ro1xz5t4LJcgRKmNLJpiHRRukVACS2V\",\"type\":4,\"version\":2,\"timestamp\":1528836856675}"
]
```

`secret` shoul be a 10+ char string, `wavesAmount` and `bitcoinAmount` - amounts in satoshis/waveslets, `wavesUserWavesPrivateKey` and `wavesTmpPrivateKey` - valid waves private keys (base58), `wavesUserBitcoinPrivateKeyWIF` - bitcoin private key in WIF format, `bitcoinUserWavesPublicKey` - valid waves public key, `bitcoinInputInfo` - info about spended bitcoin tx output (`bitcoinAmount` should be equals to the amount of this output), `txId` - tx id (hex), `outputIndex` - output index in the tx (from 0), `bitcoinUserPrivateKeyWIF` - bitcoin private key in WIF format.

In the responce you will receive 7 transactions.

```
TX1 - Alice Waves -> script1 money to tmp account + TX1-1 fee + TX3 fee
TX1-1 - script1 - set script to tmp account

TX2 - Bob Bitcoin -> script2 - fee

TX3 - Bob or anyone [normal case] : script1 -> Bob Waves address - fee
TX4 - Alice [normal case] : script2 -> Alice Bitcoin address

TX5 - Alice or anyone [failed case] : script1 (TX1-1) -> Alice Waves address
TX6 - Bob [failed case] : script2 (TX1) -> Bob Bitcoin address
```

In a good case, only transactions 1-4 should be sent, and in a bad case, 1-2 and 5-6. Or not be sent at all. Or if only 1 has been sent, then it should has been canceled using 5 , and if 2 - then 6.

Of course, in the future, private keys will not be sent to the service, only signed data. Transactions will be sent and their status will be tracked by the backend and users will receive information about the status of the exchange. Most likely this will be realized in the form of an exchange with applications for an exchange. But for now it's just a demo. 

# Analysis of atomic swap ideas

## 1. Simple P2P case

Pros:
- No trusted intermediary
Minuses:
- You need a direct data exchange channel. How do they find out about each other? What do they want to change and at what price? Still need a certain match with information who (on ip?) And where, how much wants to change.
- Both participants need to monitor both blockchains (have their own nodes or trust public ones).
- In the case of a timeout of the other, one of them can take money away.

![Simple p2p atomic swap](https://lh3.googleusercontent.com/tCEr_NtksYYJG5-OlhAUnkLyyk35AbwFVKiHfCdXXcNwnZ8mEP58cNzniOsevlSysVyJoV5tFb9nRGRLVoCa1woadj_tRxOFvgjg7H_dbzSaScqOgTs_ZvXckbc1-CJniNO1WxRR)

## Atomic swap with centralized backend

Pros:

- Do not need a direct data exchange channel. All information about the exchange they learn the same through this backend and are exchanged only with it.
- Both participants do not need to monitor both blockchains. They transfer signed transactions to a trusted backend and can leave offline (for some time).
- In the case of a timeout, neither of them can withdraw money, followed by a trusted backend and will return them earlier.
- This backend can not take money anywhere, can only fail to complete the exchange or complete it.

Minuses:

- Trusted intermediary can slip their public keys (we can somehow fix it)

![Our atomic exchange](https://lh6.googleusercontent.com/x2Jxqe46SIoMp9OsJlwv-81AqKOMAmm0Zg1z8ONe5k5t28mhn-gvk614k9oaZYQCg3v3zBhuVra4wkGetvXsu6VPfzp9RoBfoX7bxccjwtg4ov7v5wFZEpykbH58rHY1o_WKBxAY)

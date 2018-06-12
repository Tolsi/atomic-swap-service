package com.wavesplatform.atomicswap

object Server extends App {
  HttpService.startServer("localhost", 8080)
}

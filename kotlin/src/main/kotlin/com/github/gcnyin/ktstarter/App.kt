package com.github.gcnyin.ktstarter

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.kotlin.coroutines.await
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import mu.KotlinLogging

suspend fun main(args: Array<String>) {
  val name = if (args.isEmpty()) "http" else args[0]
  val logger = KotlinLogging.logger {}
  logger.info { "start" }
  val manager = IgniteClusterManager()
  val options = VertxOptions().setClusterManager(manager)
  val vertx = Vertx.clusteredVertx(options).await()
  vertx.eventBus().registerDefaultCodec(LoggingMessage::class.java, LoggingMessageCodec())
  if (name == "http") {
    val port = if (args.size >= 2) Integer.parseInt(args[1]) else 8081
    vertx.deployVerticle(HttpServerVerticle(port)).await()
  } else if (name == "logging") {
    vertx.deployVerticle(LoggingVerticle()).await()
  }
  logger.info { "deployed the verticle" }
}

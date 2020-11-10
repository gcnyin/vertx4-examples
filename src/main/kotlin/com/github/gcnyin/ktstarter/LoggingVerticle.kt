package com.github.gcnyin.ktstarter

import io.vertx.kotlin.coroutines.CoroutineVerticle
import mu.KotlinLogging

class LoggingVerticle : CoroutineVerticle() {
  private val logger = KotlinLogging.logger {}

  override suspend fun start() {
    val eventBus = vertx.eventBus()
    eventBus.consumer<String>("logging") { message ->
      logger.info { message.body() }
    }
  }
}

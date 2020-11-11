package com.github.gcnyin.ktstarter

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.kotlin.coroutines.CoroutineVerticle
import mu.KotlinLogging

data class LoggingMessage(val message: String)

class LoggingMessageCodec : MessageCodec<LoggingMessage, LoggingMessage> {
  override fun encodeToWire(buffer: Buffer?, s: LoggingMessage?) {
    if (s == null) {
      return
    }
    buffer?.appendInt(s.message.length)
    buffer?.appendString(s.message)
  }

  override fun decodeFromWire(pos: Int, buffer: Buffer?): LoggingMessage {
    if (buffer == null) {
      throw RuntimeException("buffer is null")
    }
    val length = buffer.getInt(pos)
    val message = buffer.getString(pos + 4, pos + 4 + length)
    return LoggingMessage(message)
  }

  override fun transform(s: LoggingMessage?): LoggingMessage {
    if (s == null) {
      throw RuntimeException("LoggingMessage is null")
    }
    return s
  }

  override fun name(): String {
    return this.javaClass.simpleName
  }

  override fun systemCodecID(): Byte {
    return -1
  }
}

class LoggingVerticle : CoroutineVerticle() {
  private val logger = KotlinLogging.logger {}

  override suspend fun start() {
    vertx.eventBus().consumer<LoggingMessage>("logging") { message ->
      logger.info { message.body().message }
    }
  }
}

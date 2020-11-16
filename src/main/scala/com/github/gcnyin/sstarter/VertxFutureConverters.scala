package com.github.gcnyin.sstarter

import scala.jdk.javaapi


object VertxFutureConverters {

  implicit class CompletionStageOps[T](private val cs: io.vertx.core.Future[T]) extends AnyVal {
    def asScala: scala.concurrent.Future[T] = javaapi.FutureConverters.asScala(cs.toCompletionStage)
  }

}

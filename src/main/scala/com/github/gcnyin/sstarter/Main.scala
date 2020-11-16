package com.github.gcnyin.sstarter

import com.github.gcnyin.sstarter.VertxFutureConverters.CompletionStageOps
import io.vertx.core.Vertx

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val vertx: Vertx = Vertx.vertx()
    val future = vertx.createHttpServer().requestHandler(request => {
      val response = request.response()
      response.putHeader("content-type", "application/json")
      response.`end`("""{"message":"ok"}""")
    }).listen(8081).asScala
    future onComplete {
      case Success(value) => println(value)
      case Failure(exception) => println("An error has occurred: " + exception.getMessage)
    }
  }
}

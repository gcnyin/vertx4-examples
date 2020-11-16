package com.github.gcnyin.sstarter

import com.github.gcnyin.sstarter.VertxFutureConverters.CompletionStageOps
import io.vertx.core.{AbstractVerticle, Handler, Promise, Vertx}
import io.vertx.ext.web.{Router, RoutingContext}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val vertx: Vertx = Vertx.vertx()
    val future = vertx.deployVerticle(new HttpVerticle(8081)).asScala
    future onComplete {
      case Success(value) => println(value)
      case Failure(_) => vertx.close()
    }
  }
}

class HttpVerticle(val port: Int) extends AbstractVerticle {

  override def start(startPromise: Promise[Void]): Unit = {
    val router = Router.router(vertx)
    router.get("/info").handler(InfoGetHandler)
    val future = vertx.createHttpServer().requestHandler(router).listen(port).asScala
    future onComplete {
      case Success(value) =>
        println("verticle successfully deployed, " + value)
        startPromise.complete()
      case Failure(exception) =>
        println("deployment failed: " + exception.getMessage)
        startPromise.fail(exception)
    }
  }
}

object InfoGetHandler extends Handler[RoutingContext] {
  override def handle(ctx: RoutingContext): Unit = {
    val response = ctx.response()
    response.putHeader("content-type", "application/json")
    response.`end`("""{"message":"ok"}""")
  }
}

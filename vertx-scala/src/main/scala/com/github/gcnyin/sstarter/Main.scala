package com.github.gcnyin.sstarter

import com.github.gcnyin.sstarter.VertxFutureConverters.CompletionStageOps
import com.typesafe.scalalogging.Logger
import io.vertx.core.{AbstractVerticle, Handler, Promise, Vertx}
import io.vertx.ext.web.{Router, RoutingContext}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main {
  private val logger: Logger = Logger(Main.getClass)

  def main(args: Array[String]): Unit = {
    val vertx: Vertx = Vertx.vertx()
    val future = vertx.deployVerticle(new HttpVerticle(8082)).asScala
    future onComplete {
      case Success(value) =>
        logger.info(s"deployed verticle $value")
      case Failure(exception) =>
        logger.error("deployment failed", exception)
        vertx.close()
    }
  }
}

class HttpVerticle(val port: Int) extends AbstractVerticle {
  private val logger: Logger = Logger(classOf[HttpVerticle])

  override def start(startPromise: Promise[Void]): Unit = {
    val router = Router.router(vertx)
    router.get("/info").handler(InfoGetHandler)
    val future = vertx.createHttpServer().requestHandler(router).listen(port).asScala
    future onComplete {
      case Success(_) =>
        logger.info(s"listen on http://127.0.0.1:$port")
        startPromise.complete()
      case Failure(exception) =>
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

package com.github.gcnyin.ktstarter

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import mu.KotlinLogging

suspend fun main(args: Array<String>) {
  val logger = KotlinLogging.logger {}
  logger.info { "start" }
  val port = if (args.isNotEmpty()) Integer.getInteger(args[0]) else 8080
  val vertx = Vertx.vertx()
  vertx.deployVerticle(HttpServerVerticle(port)).await()
  logger.info { "deployed the verticle" }
}

class HttpServerVerticle(private val port: Int) : CoroutineVerticle() {
  private val logger = KotlinLogging.logger {}

  override suspend fun start() {
    val connectOptions = MySQLConnectOptions().setPort(3306).setHost("localhost")
      .setDatabase("testdb").setUser("admin").setPassword("password")
    val poolOptions = PoolOptions().setMaxSize(5)
    val mySQLPool = MySQLPool.pool(vertx, connectOptions, poolOptions)
    val router = Router.router(vertx)
    router.get("/user/:userId").handler(UserQueryHandler(mySQLPool, vertx.dispatcher()))
    router.errorHandler(500) { ctx ->
      ctx.response().end("error")
    }
    val httpServer = vertx.createHttpServer()
    httpServer.requestHandler(router).listen(port).await()
    logger.info { "listen on $port port" }
  }
}

package com.github.gcnyin.ktstarter

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import mu.KotlinLogging

fun main(args: Array<String>) {
  val logger = KotlinLogging.logger {}
  val vertx = Vertx.vertx()
  val connectOptions = MySQLConnectOptions().setPort(3306).setHost("localhost")
    .setDatabase("testdb").setUser("admin").setPassword("password")
  val poolOptions = PoolOptions().setMaxSize(5)
  val mySQLPool = MySQLPool.pool(connectOptions, poolOptions)

  val router = Router.router(vertx)
  router.get("/user/:userId").handler(UserQueryHandler(mySQLPool, vertx.dispatcher()))
  router.errorHandler(500) { ctx ->
    ctx.response().end("error")
  }
  val httpServer = vertx.createHttpServer()
  httpServer.requestHandler(router).listen(8080) { logger.info { "listen on 8080 port" } }
}

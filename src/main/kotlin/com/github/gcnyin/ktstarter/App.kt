package com.github.gcnyin.ktstarter

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions

fun main(args: Array<String>) {
  val vertx = Vertx.vertx()
  val connectOptions = MySQLConnectOptions().setPort(3306).setHost("localhost")
    .setDatabase("testdb").setUser("admin").setPassword("password")
  val poolOptions = PoolOptions().setMaxSize(5)
  val mySQLPool = MySQLPool.pool(connectOptions, poolOptions)

  val router = Router.router(vertx)
  router.get("/user/:userId").handler(UserQueryHandler(mySQLPool))
  router.errorHandler(500) { ctx ->
    ctx.response().end("error")
  }
  val httpServer = vertx.createHttpServer()
  httpServer.requestHandler(router).listen(8080)
}

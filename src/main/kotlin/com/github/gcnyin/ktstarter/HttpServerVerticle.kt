package com.github.gcnyin.ktstarter

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

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

class UserQueryHandler(private val mySQLPool: MySQLPool, override val coroutineContext: CoroutineContext) : Handler<RoutingContext>, CoroutineScope {
  private val logger = KotlinLogging.logger {}

  override fun handle(ctx: RoutingContext?) {
    if (ctx == null) {
      return
    }
    val userId = ctx.pathParam("userId")
    if (userId == null) {
      ctx.response().end("Invalid userId");
      return
    }
    launch {
      try {
        val rows = mySQLPool.query("select id, name from user where id = '$userId'").execute().await()
        if (rows.size() < 1) {
          val s = "user $userId not found"
          logger.info { s }
          ctx.response().setStatusCode(404).end(s)
          return@launch
        }
        val row = rows.elementAt(0)
        val id = row?.getInteger(0)
        val name = row?.getString(1)
        val user = id?.let { name?.let { it1 -> User(it, it1) } }
        val userString = mapper.writeValueAsString(user)
        val response = ctx.response()
        response.putHeader("content-type", "application/json")
        response.end(userString)
        ctx.vertx().eventBus().send("logging", "query user $id")
      } catch (e: Exception) {
        logger.error(e) { "Internal error" }
        ctx.response().setStatusCode(500).end("Internal error")
      }
    }
  }
}

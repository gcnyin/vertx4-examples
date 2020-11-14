package com.github.gcnyin.ktstarter

import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.TimeoutHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Pool
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
    router.get("/user/:userId").handler(TimeoutHandler.create(10000)).handler(UserQueryHandler(mySQLPool, vertx.dispatcher()))
    router.post("/user")
      .handler(TimeoutHandler.create(10000))
      .handler(BodyHandler.create())
      .handler(UserPostHandler(mySQLPool, vertx.dispatcher()))
    router.errorHandler(500) { ctx ->
      ctx.response().putHeader("content-type", "application/json")
      ctx.response().end(JsonObject().put("message", "unknown error").encode())
    }
    val httpServer = vertx.createHttpServer()
    httpServer.requestHandler(router).listen(port).await()
    logger.info { "listen on $port port" }
  }
}

class UserQueryHandler(private val sqlPool: Pool, override val coroutineContext: CoroutineContext) : Handler<RoutingContext>, CoroutineScope {
  private val logger = KotlinLogging.logger {}
  private val invalidUserId = json { obj("message" to "invalid user id").encode() }
  private val internalError = json { obj("message" to "internal error").encode() }

  override fun handle(ctx: RoutingContext?) {
    if (ctx == null) {
      return
    }
    val userId = ctx.pathParam("userId")
    val response = ctx.response().putHeader("content-type", "application/json")
    if (userId == null) {
      response.end(invalidUserId)
      return
    }

    launch {
      try {
        val rows = sqlPool.query("select id, name from user where id = '$userId'").execute().await()
        if (rows.size() < 1) {
          val message = "user $userId not found"
          logger.info { message }
          response.setStatusCode(404).end(json { obj("message" to message).encode() })
          return@launch
        }
        val row = rows.elementAt(0)
        val id = row?.getInteger(0)
        val name = row?.getString(1)
        val user = id?.let { name?.let { it1 -> User(it, it1) } }
        val userString = mapper.writeValueAsString(user)
        response.end(userString)
        ctx.vertx().eventBus().send("logging", LoggingMessage("query user $id"))
      } catch (e: Exception) {
        logger.error(e) { "Internal error" }
        response.setStatusCode(500).end(internalError)
      }
    }
  }
}

class UserPostHandler(private val sqlPool: Pool, override val coroutineContext: CoroutineContext) : Handler<RoutingContext>, CoroutineScope {
  private val logger = KotlinLogging.logger {}
  private val bodyEmptyResponse = json { obj("message" to "request body is empty").encode() }
  private val usernameAlreadyExistsResponse = json { obj("message" to "username already exists").encode() }
  private val internalError = json { obj("message" to "internal error").encode() }

  override fun handle(ctx: RoutingContext?) {
    if (ctx == null) {
      return
    }

    val body = ctx.bodyAsJson
    val response = ctx.response().putHeader("content-type", "application/json")

    launch {
      try {
        val username = body.getString("username")
        if (body.isEmpty || username == null) {
          response.setStatusCode(400).end(bodyEmptyResponse)
        }
        val existResult = sqlPool.query("select 1 from user where name = '$username'").execute().await()
        if (existResult.size() >= 1) {
          response.setStatusCode(400).end(usernameAlreadyExistsResponse)
          return@launch
        }
        sqlPool.query("insert into user (name) values ('$username')").execute().await()
        val idResult = sqlPool.query("select id from user where name = '$username'").execute().await()
        val row = idResult.elementAt(0)
        val id = row.getInteger("id")
        response.end(json { obj("id" to id, "username" to username).encode() })
        logger.info { "user created: $username" }
      } catch (e: Exception) {
        logger.error(e) { "Internal error" }
        response.setStatusCode(500).end(internalError)
      }

    }
  }

}

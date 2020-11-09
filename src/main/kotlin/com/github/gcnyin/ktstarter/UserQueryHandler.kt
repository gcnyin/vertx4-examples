package com.github.gcnyin.ktstarter

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import io.vertx.mysqlclient.MySQLPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

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
      } catch (e: Exception) {
        logger.error(e) { "Internal error" }
        ctx.response().setStatusCode(500).end("Internal error")
      }
    }
  }
}

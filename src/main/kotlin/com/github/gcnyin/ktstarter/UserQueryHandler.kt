package com.github.gcnyin.ktstarter

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.mysqlclient.MySQLPool

class UserQueryHandler(var mySQLPool: MySQLPool) : Handler<RoutingContext> {
  override fun handle(ctx: RoutingContext?) {
    if (ctx == null) {
      return
    }
    val userId = ctx.pathParam("userId")
    if (userId == null) {
      ctx.response().end("Invalid userId");
      return
    }
    mySQLPool
      .query("select id, name from user where id = $userId")
      .execute { ar ->
        if (ar.succeeded()) {
          val rows = ar.result()
          if (rows.size() < 1) {
            ctx.response().setStatusCode(404).end("user $userId not found")
            return@execute
          }
          val row = rows.elementAt(0)
          val id = row?.getInteger(0)
          val name = row?.getString(1)
          val user = id?.let { name?.let { it1 -> User(it, it1) } }
          val userString = mapper.writeValueAsString(user)
          val response = ctx.response()
          response.putHeader("content-type", "application/json")
          response.end(userString)
        } else {
          ar.cause().printStackTrace();
          ctx.response().setStatusCode(500).end("error")
        }
      }
  }
}

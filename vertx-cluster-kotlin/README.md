# vertx cluster kotlin

Including

- vertx
- vertx web
- vertx cluster
- vertx mysql client
- vertx eventbus
- vertx eventbus custom message class
- ignite cluster manager
- kotlin coroutine

## Requirements

- java8+
- mysql
- ignite

## Setup environment

```
docker-compose up -d
```

## Build

```
./gradlew build
```

## Start

Start httpserver verticle

```
java -jar vertx-cluster-kotlin/build/libs/vertx-cluster-kotlin-all.jar http
```

Start logging verticle

```
java -jar vertx-cluster-kotlin/build/libs/vertx-cluster-kotlin-all.jar logging
```

Try

```
curl localhost:8081/user/1
```

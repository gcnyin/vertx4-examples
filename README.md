# Vertx Kotlin starter

A project for practicing vertx and kotlin, including

- vertx
    - eventbus
    - ignite cluster
    - mysql client
- kotlin
    - coroutine

## Requirements

- java 8+
- ignite

## Build

```
./gradlew build
```

## Run

Setup mysql and ignite

```
docker-compose up -d
```

Start http server

```
java -jar build/libs/vertx-kotlin-starter-all.jar http
```

Start logging verticle

```
java -jar build/libs/vertx-kotlin-starter-all.jar logging
```

# Vertx starter

A project for practicing vertx, kotlin and scala, including

- vertx
    - eventbus
    - eventbus custom message class
    - ignite cluster
    - mysql client
- kotlin
    - coroutine
- scala
    - vertx basic usage

## Requirements

- java 8+
- ignite
- mysql

## Build

```
./gradlew build
```

## Run

### Scala demo

```
java -jar scala/build/libs/scala-all.jar
```

### Kotlin demo

Setup mysql and ignite

```
docker-compose up -d
```

Start http server

```
java -jar kotlin/build/libs/kotlin-all.jar http
```

Start logging verticle

```
java -jar kotlin/build/libs/kotlin-all.jar logging
```

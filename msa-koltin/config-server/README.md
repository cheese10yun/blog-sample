# 스프링 클라우드 컨피그 서버

## 스프링 클라우드 컨피그 서버란 ?

스프링 클라우드 컨피그 서버 다양한 백엔드와 함께 일반적인 구성 관리 솔루션을 제공하는 오픈 소스 프로젝트로 Git, 유레카 및 콘솔 같은 벡엔드와 통합이 가능 하다. 비분산 키-값 저장소 형태이며, 스프링 및 스프링 기반이 아닌 서비스와 통합이 가능하다. 쉽게 정리해서 **스프링 클라우드 컨피그 서버 : 버전 관리 리포지토리로 백업된 중앙 집중식 구성 노출을 지원한다.**

## Code

```kotlin
@SpringBootApplication
@EnableConfigServer
class ConfigServerApplication

fun main(args: Array<String>) {
    runApplication<ConfigServerApplication>(*args)
}
```
* `@EnableConfigServer`은 서비스를 스플힝 클라ㅣ우드 컨피르 서비스로 사용을 가능하도록 한다

```yml
server:
    port: 8888

spring:
    profiles:
        active: native

    cloud:
        config:
            server:
                native:
                    search-locations: file:///Users/yun.cheese/yun/blog-sample/msa-koltin/config-server/src/main/resources/config/licensingservice
```
* `port: 8888` 일반적으로 config-server port는 8888을 사용합니다.
* `search-locations` 으로 실제 file 경로를 작성합니다.


```yml
# /Users/yun.cheese/yun/blog-sample/msa-koltin/config-server/src/main/resources/config/licensingservice/licensingservice.yml
# /Users/yun.cheese/yun/blog-sample/msa-koltin/config-server/src/main/resources/config/licensingservice/licensingservice-dev.yml

example.property: "I AM IN THE DEFAULT"
spring.jpa.database: "POSTGRESQL"
spring.datasource.platform:  "postgres"
spring.jpa.show-sql: "true"
spring.database.driverClassName: "org.postgresql.Driver"
spring.datasource.url: "jdbc:postgresql://database:5432/eagle_eye_{env}" // env에 따라 다름
spring.datasource.username: "postgres"
spring.datasource.password: "{cipher}4788dfe1ccbe6485934aec2ffeddb06163ea3d616df5fd75be96aadd4df1da91"
spring.datasource.testWhileIdle: "true"
spring.datasource.validationQuery: "SELECT 1"
spring.jpa.properties.hibernate.dialect: "org.hibernate.dialect.PostgreSQLDialect"
redis.server: "redis"
redis.port: "6379"
signing.key: "345345fsdfsf5345"
```
* 출력 확인을 위한 `licensingservice.yml` 파일을 생성합니다.

## Request
```
GET http://localhost:8888/licensingservice/default

HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Wed, 29 Jul 2020 15:05:20 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "name": "licensingservice",
  "profiles": [
    "default"
  ],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "file:///Users/yun.cheese/yun/blog-sample/msa-koltin/config-server/src/main/resources/config/licensingservice/licensingservice.yml",
      "source": {
        "example.property": "I AM IN THE DEFAULT",
        "spring.jpa.database": "POSTGRESQL",
        "spring.datasource.platform": "postgres",
        "spring.jpa.show-sql": "true",
        "spring.database.driverClassName": "org.postgresql.Driver",
        "spring.datasource.url": "jdbc:postgresql://database:5432/eagle_eye_local",
        "spring.datasource.username": "postgres",
        "spring.datasource.testWhileIdle": "true",
        "spring.datasource.validationQuery": "SELECT 1",
        "spring.jpa.properties.hibernate.dialect": "org.hibernate.dialect.PostgreSQLDialect",
        "redis.server": "redis",
        "redis.port": "6379",
        "signing.key": "345345fsdfsf5345",
        "spring.datasource.password": "4788dfe1ccbe6485934aec2ffeddb06163ea3d616df5fd75be96aadd4df1da91"
      }
    }
  ]
}


GET http://localhost:8888/licensingservice/dev

HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Wed, 29 Jul 2020 15:05:40 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "name": "licensingservice",
  "profiles": [
    "dev"
  ],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "file:///Users/yun.cheese/yun/blog-sample/msa-koltin/config-server/src/main/resources/config/licensingservice/licensingservice-dev.yml",
      "source": {
        "example.property": "I AM IN THE DEFAULT",
        "spring.jpa.database": "POSTGRESQL",
        "spring.datasource.platform": "postgres",
        "spring.jpa.show-sql": "true",
        "spring.database.driverClassName": "org.postgresql.Driver",
        "spring.datasource.url": "jdbc:postgresql://database:5432/eagle_eye_dev",
        "spring.datasource.username": "postgres",
        "spring.datasource.testWhileIdle": "true",
        "spring.datasource.validationQuery": "SELECT 1",
        "spring.jpa.properties.hibernate.dialect": "org.hibernate.dialect.PostgreSQLDialect",
        "redis.server": "redis",
        "redis.port": "6379",
        "signing.key": "345345fsdfsf5345",
        "spring.datasource.password": "4788dfe1ccbe6485934aec2ffeddb06163ea3d616df5fd75be96aadd4df1da91"
      }
    },
    {
      "name": "file:///Users/yun.cheese/yun/blog-sample/msa-koltin/config-server/src/main/resources/config/licensingservice/licensingservice.yml",
      "source": {
        "example.property": "I AM IN THE DEFAULT",
        "spring.jpa.database": "POSTGRESQL",
        "spring.datasource.platform": "postgres",
        "spring.jpa.show-sql": "true",
        "spring.database.driverClassName": "org.postgresql.Driver",
        "spring.datasource.url": "jdbc:postgresql://database:5432/eagle_eye_local",
        "spring.datasource.username": "postgres",
        "spring.datasource.testWhileIdle": "true",
        "spring.datasource.validationQuery": "SELECT 1",
        "spring.jpa.properties.hibernate.dialect": "org.hibernate.dialect.PostgreSQLDialect",
        "redis.server": "redis",
        "redis.port": "6379",
        "signing.key": "345345fsdfsf5345",
        "spring.datasource.password": "4788dfe1ccbe6485934aec2ffeddb06163ea3d616df5fd75be96aadd4df1da91"
      }
    }
  ]
}

Response code: 200; Time: 158ms; Content length: 1735 bytes
```


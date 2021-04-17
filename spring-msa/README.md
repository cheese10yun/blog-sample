# 용어

| 명칭               | 설명                                                                                                                                            |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| 라우트(Route)      | 라우트는 목적지 URI, 조건자 목록과 필터의 목록을 식별하기 위한 고유 ID로 구성된다. 라우트는 모든 조건자가 충족됐을 때만 매칭된다                |
| 조건자(Predicates) | 각 요청을 처리하기 전에 실행되는 로직, 헤더와 입력된 값 등 다양한 HTTP 요청이 정의된 기준에 맞는지를 찾는다.                                     |
| 필터(Filters)      | HTTP 요청 또는 나가는 HTTP 응답을 수정할 수 있게한다. 다운스트림 요청을 보내기전이나 후에 수정할 수 있다. 라우트 필터는 특정 라우트에 한정된다. |

# Getting Started

```
implementation("org.springframework.cloud:spring-cloud-starter-gateway")
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

```kotlin
@SpringBootApplication
class GatewayServerApplication

fun main(args: Array<String>) {
    runApplication<GatewayServerApplication>(*args)
}
```
필요한 의존성만 추가하면 빠르게 Spring Cloud Gateway를 만들 수 있습니다.


## Gateway Route 노출
```yml
management:
    endpoints:
        web:
            exposure:
                include:
                    - "gateway"
    endpoint:
        gateway:
            enabled: true
```
위에서 추가한 `actuator`의존성으로 `gateway`를 노출하면 아래처럼 url mapping 정보를 확인할 수 있습니다.

![](https://github.com/cheese10yun/blog-sample/raw/master/spring-msa/docs/images/result-1.png)

현재 아무것도 설정하지 않은 상태이기 때문에 `/actuator/gateway/routes`를 호출하면 아래와 같은 결과를 확인할 수 있습니다.

```
GET http://127.0.0.1:5555/actuator/gateway/routes

HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json

[]

Response code: 200 (OK); Time: 321ms; Content length: 2 bytes
```

## Route 설정

API를 서버를 만들고 게이트웨이와 연결해 보겠습니다.

```yml
    cloud:
        gateway:
            routes:
                -   id: order-service
                    uri: http://localhost:8181
                    predicates:
                        - Path=/order/**
                    filters:
                        - RewritePath=/order/(?<path>.*),/$\{path}
                
                -   id: cart-service
                    uri: http://localhost:8181
                    predicates:
                        - Path=/cart/**
                    filters:
                        - RewritePath=/cart/(?<path>.*),/$\{path}
```
* id: 해당 라우트의 고유 식별자를 나타냅니다.
* uri: 해당 라우터의 주소를 나타냅니다.
* predicates: 해당 라우터의 조건을 작성, `/order/**`으로 시작하는 요청의 경우 해당 라우터로 요청을 보냄
* filters: 해당 라우터의 필터로, RewritePath는 강제로 Patch를 다시 작성합니다.



## 연결할 API Server

`cart-service`, `order-service` 2 개의 API 서버를 구성합니다. 각 포트의 설정은 `cloud.gateway.routes`에 등록된 포트를 설정합니다.

## order-service
```kotlin
@RestController
@RequestMapping("/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {

    @GetMapping
    fun getOrders(pageable: Pageable) = orderRepository.findAll(pageable)
}

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "product_id", nullable = false)
    val productId: Long
) : EntityAuditing() {
    @Column(name = "order_number", nullable = false)
    val orderNumber: String = UUID.randomUUID().toString()
}
```
## cart-service

```kotlin
@RestController
@RequestMapping("/carts")
class CartApi(
    private val cartRepository: CartRepository
) {
    @GetMapping
    fun getCarts(pageable: Pageable) = cartRepository.findAll(pageable)
}

@Entity
@Table(name = "cart")
class Cart(
    @Column(name = "product_id", nullable = false)
    var productId: Long
) : EntityAuditing()
```

## Router 확인
`actuator/gateway/routes` 확인을 해보면 위에서 설정한 라우터를 확인할 수 있습니다.

```
GET http://127.0.0.1:5555/actuator/gateway/routes

HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json

[
  {
    "predicate": "Paths: [/order/**], match trailing slash: true",
    "route_id": "order-service",
    "filters": [
      "[[RewritePath /order/(?<path>.*) = '/${path}'], order = 1]"
    ],
    "uri": "http://localhost:8181",
    "order": 0
  },
  {
    "predicate": "Paths: [/cart/**], match trailing slash: true",
    "route_id": "cart-service",
    "filters": [
      "[[RewritePath /cart/(?<path>.*) = '/${path}'], order = 1]"
    ],
    "uri": "http://localhost:8181",
    "order": 0
  }
]

Response code: 200 (OK); Time: 207ms; Content length: 404 bytes
```
## 연결된 서비스 확인

```
GET http://localhost:5555/order/orders?page=0&size=5

HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 1075
Date: Sat, 22 Aug 2020 09:29:57 GMT
CUSTOM-RESPONSE-HEADER: It worked

{
  "content": [
    {
      "productId": 1,
      "id": 1,
      "createdAt": "2020-08-22T17:19:08.038",
      "updatedAt": "2020-08-22T17:19:08.038",
      "orderNumber": "7d684c44-1ea3-4dc4-9247-12c351606df3"
    },
    ...
  ],
  "pageable": {
    ...
  },
  "last": false,
  ...
}

Response code: 200 (OK); Time: 168ms; Content length: 1075 bytes
```

게이트웨이 `/order/orders?page=0&size=5`를 호출하면 `filters.RewritePath`에 의해서 `orders?page=0&size=5`를 호출하게 됩니다. 즉 라우터에 등록된 `order-service`를 호출하게 됩니다.

# Predicates

Predicates는 조건으로서 해당 라우터에 라우팅 될 조건을 표시합니다. 위 예제에서는 `Path=/order/**`, `Path=/cart/**`으로 해당 path로 들어오는 경우 해당 라우터로 라우팅 됩니다. 그 밖에도 여러 가지를 지원합니다. 대표적인 몇 개를 정리해보았습니다. 날짜 관련 매개변수는 `ZonedDateTime`를 사용해야 합니다.

## After
```yml
routes:
    -   id: order-service
        uri: http://localhost:8181
        predicates:
            - Path=/order/**
            - After=2020-08-23T19:25:19.126+09:00[Asia/Seoul]
```

`After`는 특정 날짜 이후에 호출이 가능합니다. 현재 날짜가 `After`에서 지정한 날짜 보다 이후 이어야 합니다. 서비스에 대한 이벤트 API 등 특정 시점에 Open 시킬 API가 있다면 유용합니다.

```
GET http://localhost:5555/order/orders?page=0&size=5

HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 141

{
  "timestamp": "2020-08-22T10:37:11.955+00:00",
  "path": "/order/orders",
  "status": 404,
  "error": "Not Found",
  "message": null,
  "requestId": "9b635742-2"
}

Response code: 404 (Not Found); Time: 28ms; Content length: 141 bytes
```
현재 시각 `2020-08-22T19:25:19.126+09:00[Asia/Seoul]` 이라면 `HTTP/1.1 404 Not Found`을 응답 받게됩니다.

## Before
```yml
routes:
    -   id: order-service
        uri: http://localhost:8181
        predicates:
            - Path=/order/**
            - Before=2020-08-20T19:25:19.126+09:00[Asia/Seoul]
```
`Before`는 특정 날짜 이전 호출이 가능합니다. 현재 날짜가 `Before`에서 지정한 날짜 보다 이전 이어야 합니다. 특정 API가 deprecate가 되는 경우 유용합니다.

## Between
```yml
routes:
    -   id: order-service
        uri: http://localhost:8181
        predicates:
            - Path=/order/**
            - Between=2020-08-17T19:25:19.126+09:00[Asia/Seoul], 2020-08-20T19:25:19.126+09:00[Asia/Seoul]
```
`Between`는 특정 날짜 사이에만 호출이 가능합니다. 특정 기간에만 사용하는 이벤트 API 등에 사용하면 유용합니다.


## Weight

```yml
routes:
    -   id: order-service-high
        uri: http://localhost:8181
        predicates:
            - Path=/order/**
            - Weight=group-order, 7
        filters:
            - RewritePath=/order/(?<path>.*),/$\{path}

    -   id: order-service-low
        uri: http://localhost:8787
        predicates:
            - Path=/order/**
            - Weight=group-order, 3
        filters:
            - RewritePath=/order/(?<path>.*),/$\{path}
```
`group`, `weight`를 기반으로 그룹별로 가중치를 계산하게 됩니다. 위 설정은 70% `order-service-high`, 30% `order-service-low`으로 라우팅을 분배합니다.

# Filters

HTTP Request, Reponse에 대한 수정을 할 수 있습니다. 특정 라우터에에서 안에서 동작하게 됩니다.


## RewritePath

RewritePath는 HTTP Request를 수정하여 특정 Server에 전달하게 됩니다. 정규표현식을 사용해서 유연하게 HTTP Request Path를 변경합니다.

```yml
 routes:
     -   id: order-service
         uri: http://localhost:8181    
         filters:
             - RewritePath=/order/(?<path>.*),/$\{path}
```

`RewritePath`를 통해서 `/order/orders` -> `/order/orders`으로 재작성합니다. 즉, `/order/orders?page=0&size=5` 요청이 오면 `/order/`를제거하고 `orders?page=0&size=5`를 기반으로 `order-service`를 호출하게 됩니다.


## Retry

| name       | 설명                                                                              | 기본값                            |
| ---------- | --------------------------------------------------------------------------------- | --------------------------------- |
| retries    | 재시도 횟수                                                                       | 3번                               |
| statuses   | 재시도해야하는 HTTP 상태 코드(`org.springframework.http.HttpStatus`)              | -                                 |
| series     | 재시도해야하는 HTTP 상태 코드시리즈(`org.springframework.http.HttpStatus.Series`) | 5XX                               |
| methods    | 재시도해야하는 HTTP 메소드(`org.springframework.http.HttpMethod`)                 | GET                               |
| exceptions | 재시도해야하는 Exception                                                          | `IOException`, `TimeoutException` |
| backoff    | 재시도하는 시간텀 지정 `firstBackoff * (factor ^ n)` n번 반복                     | 비활성화                          |


```yml
spring:
    cloud:
        gateway:
            discovery:
                locator:
                    enabled: true
            routes:
                -   id: order-service
                    uri: lb://order-service
                    predicates:
                        - Path=/order/**
                    filters:
                        - RewritePath=/order/(?<path>.*),/$\{path}
                        -   name: Retry
                            args:
                                retries: 3
                                statuses: INTERNAL_SERVER_ERROR
                                methods: GET
                                backoff:
                                    firstBackoff: 1000ms
                                    maxBackoff: 6000ms
                                    factor: 2
                                    basedOnPreviousValue: false
```
제시도 횟수는 `retries: 3`, 재시도 HTTP Status는 `statuses: INTERNAL_SERVER_ERROR (500)`, 재시도 HTTP method는 `GET` `backoff` 설정은 `10ms(firstBackoff) * (2(factor)* 3(retries))`으로 `retries` 만큼 반복됩니다.

```kotlin
@RestController
@RequestMapping("/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {

    @GetMapping
    fun getOrders(pageable: Pageable): Page<Order> {
        println("getOrders 호출")
        if(true){
            throw RuntimeException("Error")
        }
        return orderRepository.findAll(pageable)
    }

    @GetMapping("/carts/{id}")
    fun getCarts(@PathVariable id: Long) = cartClient.getCart(id)
}
```
해당 API는 `RuntimeException("Error")`를 발생시키고 있어 Status 500을 응답합니다.

```
GET http://localhost:5555/order/orders?page=0&size=5

HTTP/1.1 500 Internal Server Error
transfer-encoding: chunked
Content-Type: application/json
Date: Sat, 22 Aug 2020 14:57:15 GMT
CUSTOM-RESPONSE-HEADER: It did not work

{
  "timestamp": "2020-08-22T14:57:15.122+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "",
  "path": "/orders"
}

Response code: 500 (Internal Server Error); Time: 7062ms; Content length: 120 bytes
```
결과를 확인하면 3번의 Retry가 있었고, 결국 500을 리턴하게 됩니다.


```
getOrders 호출
2020-08-22 23:57:08.080 ERROR [order-service,,,] 17139 --- [nio-8181-exec-7] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.RuntimeException: Error] with root cause

getOrders 호출
2020-08-22 23:57:09.091 ERROR [order-service,,,] 17139 --- [nio-8181-exec-8] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.RuntimeException: Error] with root cause

getOrders 호출
2020-08-22 23:57:11.107 ERROR [order-service,,,] 17139 --- [nio-8181-exec-9] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.RuntimeException: Error] with root cause
```
`order-service` 로그를 확인해보면 3번의 호출이 있었는지를 확인할 수 있습니다.


```kotlin
@RestController
@RequestMapping("/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {

    var errorCount = 0

    @GetMapping
    fun getOrders(pageable: Pageable): Page<Order> {
        println("getOrders 호출")
        if (errorCount < 2) {
            println("예외발생 $errorCount 1증가")
            errorCount++
            throw RuntimeException("Error")
        }
        errorCount = 0 // 초기화
        return orderRepository.findAll(pageable)
    }
}
```
해당 코드는 2번 예외가 발생하지만 3번째에서 응답을 리턴해주는 코드입니다. 재시도를 3번 실행하기 때문에 3번째에는 정상적인 응답을 받을 수 있습니다.
```
GET http://localhost:5555/order/orders?page=0&size=5

HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json
Date: Sat, 22 Aug 2020 15:13:16 GMT
CUSTOM-RESPONSE-HEADER: It worked

{
  "content": [
    {
      "productId": 1,
      "id": 1,
      "createdAt": "2020-08-23T00:11:24.747",
      "updatedAt": "2020-08-23T00:11:24.747",
      "orderNumber": "519011ff-7eaf-4e85-b48f-a9aa6ab879ac"
    }
    ...
  ],
  "pageable": {
   ...
  "totalPages": 2,
   ...
}

Response code: 200 (OK); Time: 3034ms; Content length: 1075 bytes
```
3번의 응답시간을 기다려야 하기 때문에 `3034ms` 정도 걸리는 걸 확인할 수 있습니다. 재시도는 단순 조회만 하는 GET 요청에 외에는 신중하게 선택해야 합니다. 게이트웨이에서 재시도를 진행하기 때문에 각 서비스 간의 통신에서 생성, 삭제, 수정 등 조회 조건 외에 동작이 있다면 문제가 생길 가능성이 높습니다. 또 `HTTP Status 5XX` 응답은 재시도를 하는 것은 바람직하지만, `HTTP Status 4XXX`에서는 동일한 요청이면 동일한 이유로 실패하기 때문에 재시도를 안 하는 게 더 효율적입니다. 단순 조회 용이 아니면 신중하게 사용해야 합니다.

# HTTP Timeout 설정

## 글로벌 설정
```yml
spring:
    cloud:
        gateway:
            httpclient:
                connect-timeout: 10000
                response-timeout: 10s
```

`connect-timeout` 밀리 초 단위로 지정, `response-timeout` Duration으로 지정 해야 합니다.

```kotlin

@RestController
@RequestMapping("/orders")
class OrderApi(
    private val orderRepository: OrderRepository
) {

    @GetMapping
    fun getOrders(pageable: Pageable): Page<Order> {
        Thread.sleep(1100) // timeout 발생
        return orderRepository.findAll(pageable)
    }

```
클라이언트 응답시간이 1초로 설정했기 때문에 1초를 넘어가면 아래와 같이 `HTTP/1.1 504 Gateway Timeout`응답을 확인할 수 있습니다.

```
GET http://localhost:5555/order/orders?page=0&size=5

HTTP/1.1 504 Gateway Timeout
Content-Type: application/json
Content-Length: 145

{
  "timestamp": "2020-08-22T14:05:09.267+00:00",
  "path": "/order/orders",
  "status": 504,
  "error": "Gateway Timeout",
  "message": "",
  "requestId": "0d492aaf-1"
}

Response code: 504 (Gateway Timeout); Time: 4798ms; Content length: 145 bytes
```

## 라우터별 설정

```yml
spring:
    cloud:
        gateway:
            routes:
                -   id: order-service
                    uri: lb://order-service
                    predicates:
                        - Path=/order/**
                    filters:
                        - RewritePath=/order/(?<path>.*),/$\{path}
                    metadata:
                        connect-timeout: 1000
                        response-timeout: 1000
```
`metadata`설정을 통해서 라우터별 설정을 진행할 수 있습니다. 여기서 중요한 점은 `metadata` 설정 시 `connect-timeout`, `response-timeout` 모두 밀리 초 단위로 지정해야 합니다. Global 설정과는 차이가 있습니다.

# Logging Sleuth

## Gateway Logging

```kotlin
class GatewayServerApplication

fun main(args: Array<String>) {
    System.setProperty("reactor.netty.http.server.accessLogEnabled", "true")
    runApplication<GatewayServerApplication>(*args)
}
```
Reactor Netty 액세스 로그를 활성화하려면 `System.setProperty("reactor.netty.http.server.accessLogEnabled", "true")`을 설정해야 합니다. 공식 문서에 따르면 Spring Boot 설정이 아니기 때문에 yml으로 설정하지 않고 위처럼 설정해야 한다고 합니다.

![](https://github.com/cheese10yun/blog-sample/raw/master/spring-msa/docs/images/result-2.png)

정상적으로 로킹이 되는 것을 확인할 수 있습니다.

## Sleuth

스프링 클라우드 슬루스(Sleuth)는 마이크로 서비스 환경에서 서로 다른 시스템의 요청을 연결하여 로깅을 해줄 수 있게 해주는 도구입니다. 이런 경우 슬루스를 이용해서 쉽게 요청에 대한 로깅을 연결해서 볼 수 있습니다. 또 RestTemplate, 페인 클라이언트, 메시지 채널 등등 다양한 플랫폼과 연결하기 쉽습니다. 아래 예제에서는 폐인 클라이언트와 연결해서 로깅하는 방법을 설명하겠습니다.

## 의존성 추가
```
implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
```
`gateway-server`, `order-service`. `cart-service` 모듈에 해당 의존성을 추가합니다.

```yml
cloud:
    gateway:
        discovery:
            locator:
                enabled: true
        routes:
            -   id: order-service
                uri: lb://order-service
                predicates:
                    - Path=/order/**
                filters:
                    - RewritePath=/order/(?<path>.*),/$\{path}
            
            -   id: cart-service
                uri: lb://cart-service
                predicates:
                    - Path=/cart/**
                filters:
                    - RewritePath=/cart/(?<path>.*),/$\{path}
```
`openfeign`을 이용해서 클라이언트를 호출할 것이므로 `discovery(Eureka)` 설정을 합니다.(아래 포스팅에서 유레카, 페인 관련 설정을 진행하겠습니다.) 호출 순서가 `Gateway` -> `order-service` -> `cart-service` 호출에 대한 로깅입니다.

```
# spring-gateway
2020-08-22 22:11:20.671 DEBUG [gateway-server,d1905ab24f0b5d1a,d1905ab24f0b5d1a,true] 12133 --- [ctor-http-nio-4] o.s.c.g.h.RoutePredicateHandlerMapping   : Route matched: order-service
2020-08-22 22:11:20.671 DEBUG [gateway-server,d1905ab24f0b5d1a,d1905ab24f0b5d1a,true] 12133 --- [ctor-http-nio-4] o.s.c.g.h.RoutePredicateHandlerMapping   : Mapping [Exchange: GET http://localhost:5555/order/orders/carts/2] to Route{id='order-service', uri=lb://order-service, order=0, predicate=Paths: [/order/**], match trailing slash: true, gatewayFilters=[[[RewritePath /order/(?<path>.*) = '/${path}'], order = 1], [[Retry retries = 3, series = list[SERVER_ERROR], statuses = list[502 BAD_GATEWAY], methods = list[GET, POST], exceptions = list[IOException, TimeoutException]], order = 2]], metadata={}}

# order-service
2020-08-22 22:11:20.685  INFO [order-service,d1905ab24f0b5d1a,ba6672843cb90f99,true] 11949 --- [nio-8181-exec-6] com.service.order.HttpLoggingFilter      : 
    ⊙ GET /orders/carts/2
    ├─ Headers: accept: application/json, user-agent: Apache-HttpClient/4.5.12 (Java/11.0.7), accept-encoding: gzip,deflate, custom-request-header: userName, forwarded: proto=http;host="localhost:5555";for="127.0.0.1:57509", x-forwarded-for: 127.0.0.1, x-forwarded-proto: http, x-forwarded-prefix: /order, x-forwarded-port: 5555, x-forwarded-host: localhost:5555, host: 192.168.0.5:8181, x-b3-traceid: d1905ab24f0b5d1a, x-b3-spanid: ba6672843cb90f99, x-b3-parentspanid: d1905ab24f0b5d1a, x-b3-sampled: 1, content-length: 0

# cart-service
2020-08-22 22:11:20.683  INFO [cart-service,d1905ab24f0b5d1a,716b9cd4e4e52bdf,true] 11935 --- [nio-8282-exec-5] com.service.cart.HttpLoggingFilter       : 
    ⊙ GET /carts/2
    ├─ Headers: x-b3-traceid: d1905ab24f0b5d1a, x-b3-spanid: 716b9cd4e4e52bdf, x-b3-parentspanid: ba6672843cb90f99, x-b3-sampled: 1, accept: */*, user-agent: Java/1.8.0_212, host: 192.168.0.5:8282, connection: keep-alive
```

로그를 보면 `gateway-server`에서 `traceId: d1905ab24f0b5d1a`발급하고 `order-service`에게 전달할 때 header 정보에 `x-b3-traceid: d1905ab24f0b5d1a`를 추가하고, `cart-service`도 마찬가지로 `traceId`를 전달받고 자신의 고유한 ID `x-b3-parentspanid: ba6672843cb90f99(order-service에서 전달받은)` 발급합니다. 결국 `d1905ab24f0b5d1a` 값 하나로 연결된 하나의 요청을 추적할 수 있습니다.


# Eureka & Feign & Ribbon
Spring Cloud Gateway는 유레카 연동도 손쉽게 가능합니다. 본 포스팅은 Spring Cloud Gateway에 대한 포스팅이므로 유레카에 대한 설정은 다루지 않겠습니다. 해당 내용은 실제 코드를 확인해 주세요.

![](https://github.com/cheese10yun/blog-sample/raw/master/spring-msa/docs/images/result-3.png)

`order-service`, `cart-service` 서비스를 유레카에 등록 시켰습니다. 이제 라우터에 uri를 연결하기만 하면 손쉽게 연결이 가능합니다.

```yml
gateway:
        discovery:
            locator:
                enabled: true
        routes:
            -   id: order-service
#                    uri: http://localhost:8181 # 기존 방시
                uri: lb://order-service # 유레카를 통한 방식
                predicates:
                    - Path=/order/**
                filters:
                    - RewritePath=/order/(?<path>.*),/$\{path}

            -   id: cart-service
#                    uri: http://localhost:8181 # 기존 방시
                uri: lb://cart-service # 유레카를 통한 방식
                predicates:
                    - Path=/cart/**
                filters:
                    - RewritePath=/cart/(?<path>.*),/$\{path}
```
설정은 간단합니다. `uri: lb://{service-name}`형식으로 유레카에 등록된 서비스 네임을 작성하게 되면 완료됩니다. 유레카에 등록했기 때문에 Feign, Ribbon 이용한 클라이언트 사이드 로드 밸런싱이 가능합니다.

```kotlin
@FeignClient("cart-service")
@RibbonClient("cart-service")
interface CartClient {

    @GetMapping("/carts/{id}")
    fun getCart(@PathVariable id: Long): CartResponse

    data class CartResponse(
        val productId: Long
    )
}

@RestController
@RequestMapping("/orders")
class OrderApi(
    private val cartClient: CartClient
) {

    @GetMapping("/carts/{id}")
    fun getCarts(@PathVariable id: Long) = cartClient.getCart(id)
}
```

게이트웨이를 호출해서 `order-service`를 호출하고, 페인 클라이언트를 이용해서 `cart-service`를 호출하는 것을 확인할 수 있습니다.

```
GET http://localhost:5555/order/orders/carts/2

HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 15
Date: Sat, 22 Aug 2020 13:37:48 GMT
CUSTOM-RESPONSE-HEADER: It worked

{
  "productId": 2
}

Response code: 200 (OK); Time: 109ms; Content length: 15 bytes
```

## Filter 설명

![](https://cloud.spring.io/spring-cloud-gateway/reference/html/images/spring_cloud_gateway_diagram.png)

클라이언트는 Spring Cloud Gateway를 통해 요청을 하고 게이트웨이는 매핑에서 요청이 경로와 일치한다고 판단하면 게이트웨이 웹 처리기로 요청을 전송하게 됩니다.

> [Spring Cloud Gateway Document](https://cloud.spring.io/spring-cloud-gateway/reference/html/)


![](https://github.com/cheese10yun/blog-sample/raw/master/spring-msa/docs/images/gateway-flow.png)

```kotlin
@Component
class CustomFilter : AbstractGatewayFilterFactory<CustomFilter.Config>(Config::class.java) {
    val log by logger()

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response
            log.info("CustomFilter request id: ${request.id}")
            chain.filter(exchange).then(Mono.fromRunnable { log.info("CustomFilter response status code: ${response.statusCode}") })
        }
    }

    class Config
}

@Component
class GlobalFilter : AbstractGatewayFilterFactory<GlobalFilter.Config>(Config::class.java) {
    val log by logger()

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response

            log.info("Global request id: ${request.id}")
            chain.filter(exchange).then(Mono.fromRunnable {
                log.info("Global response status code: ${response.statusCode}")
            })
        }
    }

    class Config
}
```
필터는 모두 AbstractGatewayFilterFactory를 상속받아 구현을 진행합니다. 실제 Gateay 로그는 아래와 같습니다.

![](https://github.com/cheese10yun/blog-sample/raw/master/spring-msa/docs/images/gateway-log.png)


# 출처
* [Spring Cloud Gateway  Reference](https://cloud.spring.io/spring-cloud-gateway/reference/html/)
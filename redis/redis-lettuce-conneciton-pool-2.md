# Redis Lettuce 커넥션 풀의 진짜 역할: 실제 시나리오로 이해하기

Redis는 기본적으로 싱글 스레드로 동작하는 인메모리 데이터베이스입니다. 이러한 특성 때문에 많은 개발자들은 Redis 클라이언트인 Lettuce에서 제공하는 커넥션 풀이 실제로 얼마나 유용한지, 그리고 그것이 애플리케이션에 어떤 이점을 가져다줄 수 있는지에 대해 의문을 가질 수 있습니다. 특히 "**Redis는 싱글 스레드인데, 굳이 여러 개의 커넥션을 유지하는 것이 의미가 있을까?**"라는 질문은 자주 듣는 질문 중 하나입니다.

Redis의 싱글 스레드 구조는 서버가 동시에 여러 클라이언트의 요청을 처리하는 것이 아니라, 각 요청을 순차적으로 처리한다는 것을 의미합니다. **즉, 많은 요청이 들어와도 Redis는 각 요청을 차례차례 처리하기 때문에 일반적인 데이터베이스처럼 병렬적으로 여러 쿼리를 동시에 실행할 수 있는 구조가 아닙니다. 그래서 많은 개발자들이 "그렇다면 Redis 클라이언트 측에서 커넥션 풀을 사용하는 것이 성능에 어떤 영향을 줄 수 있을까?"라는 의문을 가지게 되는 것입니다.**

하지만 Redis의 클라이언트 측, 특히 애플리케이션 레벨에서의 커넥션 관리 방식은 Redis 서버의 동작 방식과는 또 다른 관점을 제공합니다. **Lettuce 클라이언트는 기본적으로 비동기 및 넌블로킹 I/O를 지원하며, 커넥션 풀을 통해 여러 개의 커넥션을 관리함으로써 애플리케이션의 성능을 최적화할 수 있는 기회를 제공합니다. 이를 통해 Redis의 싱글 스레드 특성에도 불구하고 애플리케이션의 전반적인 응답성을 향상시킬 수 있습니다.** 이번 글에서는 Lettuce 커넥션 풀이 애플리케이션 성능에 미치는 영향을 실제 시나리오를 통해 자세히 알아보겠습니다.

## JDBC Hikari Connection Pool의 동작 방식

Redis Lettuce 커넥션 풀의 역할을 이해하기 위해, 먼저 전통적인 데이터베이스 커넥션 풀의 대표적인 예인 **JDBC Hikari Connection Pool**을 살펴보겠습니다.

```mermaid
sequenceDiagram
    participant Server
    participant MySQL
    Server ->> MySQL: 주문 정보 조회 요청
    MySQL -->> Server: 주문 정보 조회 응답 (2,500ms 소요)
```

전통적인 Hikari Connection Pool에서는 애플리케이션이 주문 정보를 조회하기 위해 커넥션 풀에서 하나의 커넥션을 가져옵니다. 아래 그림에서 볼 수 있듯이, **idleConnections**가 10개라면, 그 중 하나의 커넥션을 가져와 **activeConnections**로 전환하게 됩니다. 이 경우 **idleConnections**는 9개로 줄고, **activeConnections**는 1개가 됩니다. 전체 **totalConnections**는 변하지 않고 유지됩니다.

![Hikari Connection Pool - Idle to Active](https://raw.githubusercontent.com/cheese10yun/blog-sample/refs/heads/master/redis/docs/connection-pool-001.png)

만약 요청이 많이 들어와 응답이 지연되고 있는 경우를 생각해 봅시다. 아래 그림처럼 **maximum-pool-size**가 10개인 상황에서, 모든 10개의 커넥션이 **activeConnections**로 전환되어 사용 중이라면, 추가적인 요청은 **threadsAwaitingConnection**으로 들어가 대기하게 됩니다. 즉, 사용 가능한 커넥션이 없기 때문에 요청 스레드는 커넥션이 반환될 때까지 기다려야 합니다.

![Hikari Connection Pool - Threads Awaiting Connection](https://raw.githubusercontent.com/cheese10yun/blog-sample/refs/heads/master/redis/docs/connection-pool-002.png)

이와 같이 전통적인 커넥션 풀의 개념에서는, **요청 스레드마다 활성화된 커넥션을 사용**하며, 해당 스레드가 작업을 끝내기 전까지는 **커넥션을 점유**하게 됩니다. 이는 데이터베이스의 동시 연결 수와 처리 능력을 효과적으로 관리할 수 있는 방법이지만, 커넥션이 사용 중일 때 대기하는 요청들이 발생할 수 있다는 단점이 있습니다.

이제 이러한 전통적인 커넥션 풀과 비교하여, **Redis Lettuce Connection Pool**이 어떻게 다른 방식으로 동작하는지에 대해 알아보겠습니다.

## Redis Lettuce Connection Pool의 동작 방식

Redis Lettuce Connection Pool의 동작 방식을 이해하기 위해, **Redis 조회와 MySQL 조회가 함께 사용되는 시나리오**를 살펴보겠습니다.

```mermaid
sequenceDiagram
    participant Server
    participant Redis
    participant MySQL
    Server ->> Redis: 쿠폰 조회 요청
    Redis -->> Server: 쿠폰 조회 응답 (10ms)
    Server ->> MySQL: 주문 정보 조회 요청 (2,500ms)
    MySQL -->> Server: 주문 정보 조회 응답
    Server -->> Server: 최종 응답 반환
```

위의 시나리오에서 애플리케이션은 먼저 Redis에서 쿠폰 정보를 조회한 후, MySQL에서 주문 정보를 조회합니다. Redis 쿠폰 조회는 10ms 만에 응답이 오지만, 이후 이어지는 MySQL 조회는 2,500ms가 걸립니다. 이 상황에서 Redis Lettuce 커넥션 풀이 어떻게 동작하는지를 이해하는 것이 중요합니다.

Redis에 쿠폰 조회 요청을 보내면, 10ms 내에 쿠폰 정보가 응답됩니다. 여기서 Lettuce 커넥션 풀이 1개만 있다고 가정해 보겠습니다. 만약 이 상황이 전통적인 커넥션 풀 구조였다면, MySQL 데이터 조회(2,500ms)가 완료되기 전까지 하나뿐인 Redis 커넥션이 블록되어 Redis에 대한 추가적인 요청을 처리할 수 없었을 것입니다. 이는 Redis 서버가 이미 응답을 완료했음에도 불구하고, 애플리케이션 측에서 더 이상 Redis에 대한 요청을 처리할 수 없게 된다는 문제를 야기합니다.

그러나 Redis Lettuce의 경우 비동기적으로 동작할 수 있습니다. Redis 서버에서 응답을 내린 후 해당 커넥션이 즉시 반환된다면, MySQL 조회가 진행 중이더라도 Redis에 대한 새로운 요청을 처리할 수 있게 됩니다. 이는 Redis 서버가 싱글 스레드로 동작하더라도 Lettuce 클라이언트 측에서는 추가적인 요청을 계속해서 보낼 수 있는 가능성을 열어줍니다. 그렇다면 실제로 Redis Lettuce가 이러한 방식으로 동작하는지, 아니면 다른 방식으로 동작하는지 **코드를 통해 더 자세히 살펴보겠습니다**.

```kotlin
@RestController
@RequestMapping
class MemberController(
    private val redisConnectionPoolSample: RedisConnectionPoolSample,
) {

    @GetMapping("/api/redis")
    fun getRedis(@RequestParam("id") id: String) = redisConnectionPoolSample.getRedis(id)

    @GetMapping("/api/mysql")
    fun getMySql(@RequestParam("id") id: Long) = redisConnectionPoolSample.getMySql(id)

    @GetMapping("/api/composite")
    fun getRedis2(@RequestParam("id") id: String) = redisConnectionPoolSample.getComposite(id)
}

@Service
class RedisConnectionPoolSample(
    private val couponRepository: CouponRepository,
    private val orderRepository: OrderRepository

) {

    fun getRedis(id: String): Coupon? {
        return couponRepository.findByIdOrNull(id)
    }


    fun getMySql(id: Long): Order? {
        printHikariConnection()
        return orderRepository.findByIdOrNull(id)
    }

    fun getComposite(id: String): Pair<Coupon?, Order?> {
        val coupon = couponRepository.findByIdOrNull(id)
        val order = orderRepository.findByIdOrNull(id.toLong())
        Thread.sleep(2500) // 2.5초 대기
        printHikariConnection()
        return Pair(coupon, order)
    }

    private fun printHikariConnection() {
        val targetDataSource = dataSource.unwrap(HikariDataSource::class.java)
        val hikariDataSource = targetDataSource as HikariDataSource
        val hikariPoolMXBean = hikariDataSource.hikariPoolMXBean
        val hikariConfigMXBean = hikariDataSource.hikariConfigMXBean

        val log = buildString {
            append("totalConnections: ${hikariPoolMXBean.totalConnections}, ")
            append("activeConnections: ${hikariPoolMXBean.activeConnections}, ")
            append("idleConnections: ${hikariPoolMXBean.idleConnections}, ")
            append("threadsAwaitingConnection: ${hikariPoolMXBean.threadsAwaitingConnection}")
        }
        println(log)
    }
}
```

* /api/redis는 단순히 redis id 기준으로 조회
* /api/mysql 단순히 mysql 조회
* /api/composite는 redis 조회 후, mysql 조회 2.5초 대기 이후 응답
* Thread.sleep(2500) 대기하는 구간에서
* lettuce connection max-idle 1개, max-active 1개, hikari maximum-pool-size: 1, minimum-idle: 1 으로 설정

### 시나리오: getComposite 호출 이후 getMySql 호출

Hikari 설정은 maximum-pool-size가 1이고, minimum-idle이 1로 되어 있으며, `getComposite` 호출 시 사용 가능한 1개의 커넥션 중 하나를 사용하여 작업을 진행합니다.

이때 로그:

```txt
totalConnections: 1, activeConnections: 1, idleConnections: 0, threadsAwaitingConnection: 0
```

위 로그를 통해 알 수 있듯이, 하나의 커넥션이 사용 중이며, 대기 중인 스레드는 없습니다.

이 상태에서 `getMySql` 호출을 시도하면, 사용 가능한 **idle 커넥션**이 없기 때문에 `getComposite` 호출이 끝난 후 반환된 커넥션을 사용해야 합니다. 이로 인해 **threadsAwaitingConnection** 상태에서 대기하게 되고, 지연이 발생합니다. 이후 **threadsAwaitingConnection**에서 대기하던 요청이 **activeConnections**로 전환되면, `getMySql` 호출에서 해당 커넥션을 사용할 수 있게 됩니다.

이때 로그:

```txt
totalConnections: 1, activeConnections: 1, idleConnections: 0, threadsAwaitingConnection: 1
```

위 로그는 `getComposite` 호출로 인해 커넥션이 점유된 상태에서 `getMySql` 호출이 대기 중인 상황을 보여줍니다.

실제 응답 시간을 확인해보면, `/api/mysql`만 호출하는 경우 응답 시간이 매우 빠른 것을 확인할 수 있습니다. 예를 들어:

```
Response code: 200; Time: 24ms (24 ms); Content length: 64 bytes (64 B)
```

그러나 `/api/composite`를 호출한 이후에 `/api/mysql`를 호출하게 되면, 응답 시간이 느려지는 것을 확인할 수 있습니다.

```
Response code: 200; Time: 1112ms (1 s 112 ms); Content length: 64 bytes (64 B)
```

이는 Hikari 커넥션 풀이 스레드를 블록 시키는 방식으로 동작하기 때문이며, idle한 커넥션이 없을 경우 `threadsAwaitingConnection`에 대기 요청이 쌓이고, 앞선 커넥션들이 반환되어야 비로소 다시 `activeConnections`로 전환될 수 있기 때문입니다.

그렇다면 Redis Lettuce 커넥션 풀은 어떻게 동작하는지 살펴보겠습니다. Lettuce에서도 동일하게 스레드를 블록 시킨다면 지연이 발생할 것이며, 반대로 블록하지 않는다면 MySQL의 지연과 상관없이 추가적인 Redis 호출에 대해 빠르게 응답할 수 있을 것입니다.




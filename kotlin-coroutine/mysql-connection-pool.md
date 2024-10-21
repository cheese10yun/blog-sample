# MySQL Connection Pool 관리: HikariCP 설정 및 타임아웃 처리

## Connection Pool이란?

애플리케이션에서 MySQL 데이터베이스와의 효율적인 연결을 위해 **커넥션 풀**을 사용합니다. 커넥션 풀은 미리 일정한 수의 데이터베이스 연결을 생성해 두고, 애플리케이션이 필요할 때마다 이 연결들을 재사용함으로써 성능을 향상시키는 기법입니다. 이 방식은 데이터베이스와의 연결을 매번 새로 생성하는 대신 이미 준비된 연결을 재사용하여 애플리케이션의 응답 시간을 줄이는 데 매우 유용합니다.

## Java JDBC와 HikariCP

Java 애플리케이션에서 가장 널리 사용되는 커넥션 풀 중 하나는 **HikariCP**입니다. HikariCP는 가볍고 빠른 커넥션 풀로, 대규모 트래픽이 발생하는 상황에서도 효율적인 연결 관리를 지원합니다. HikariCP는 **HikariPoolMXBean**과 **HikariConfigMXBean**이라는 JMX(Java Management Extensions)를 통해 커넥션 풀의 상태 및 설정을 관리할 수 있습니다.

### Spring Boot에서의 HikariCP 설정과 기본값

| 설정 항목                           | 설명                                                                               | 기본값                       |
|---------------------------------|----------------------------------------------------------------------------------|---------------------------|
| **maximum-pool-size**           | 커넥션 풀에서 유지할 수 있는 최대 커넥션 수입니다. 이 수치를 초과하는 요청은 대기 상태로 들어갑니다.                       | 10                        |
| **minimum-idle**                | 풀에서 유지할 유휴 커넥션의 최소 개수입니다. 유휴 커넥션이 이 수치 이하로 떨어지면 새로운 커넥션이 생성됩니다.                  | `maximum-pool-size` 값과 동일 |
| **connection-timeout**          | 커넥션을 가져오기 위해 스레드가 대기할 수 있는 최대 시간입니다. 이 시간이 초과되면 예외가 발생합니다.                       | 30,000ms (30초)            |
| **max-lifetime**                | 커넥션이 유지될 수 있는 최대 시간입니다. 이 시간이 지나면 커넥션은 폐기되고 새 커넥션으로 교체됩니다.                       | 1,800,000ms (30분)         |
| **idle-timeout**                | 유휴 상태의 커넥션이 풀에서 유지될 수 있는 최대 시간입니다. 이 시간이 지나면 유휴 커넥션이 풀에서 제거됩니다.                  | 600,000ms (10분)           |
| **leak-detection-threshold**    | 지정된 시간(밀리초) 동안 사용되지 않은 커넥션을 감지하는 데 사용됩니다. 이 시간이 지나면 커넥션 리크(leak)를 의심하고 경고를 남깁니다. | 0 (비활성화)                  |
| **pool-name**                   | 커넥션 풀의 이름을 지정합니다. 기본적으로 HikariCP는 자동으로 이름을 생성하지만, 필요에 따라 지정할 수 있습니다.             | 자동 생성된 이름                 |
| **auto-commit**                 | 새 커넥션이 자동 커밋 모드로 시작할지를 결정합니다. 각 쿼리 후 자동으로 커밋됩니다.                                 | `true`                    |
| **validation-timeout**          | 커넥션이 유효한지 검증할 때 사용할 최대 시간입니다. 이 시간이 초과되면 커넥션은 유효하지 않다고 판단하고 폐기됩니다.               | 5,000ms (5초)              |
| **read-only**                   | 커넥션이 읽기 전용 모드에서 작동할지를 결정합니다.                                                     | `false`                   |
| **isolate-internal-queries**    | 내부 쿼리(예: 커넥션 풀의 유지 관리 쿼리)가 애플리케이션의 쿼리와 격리되는지를 설정합니다.                             | `false`                   |
| **allow-pool-suspension**       | 커넥션 풀의 일시 정지 기능을 활성화합니다. 이 설정이 활성화되면 풀을 일시 정지하거나 다시 시작할 수 있습니다.                  | `false`                   |
| **initialization-fail-timeout** | 풀을 시작할 때 초기화에 실패하는 경우를 대비한 타임아웃 시간입니다. 이 시간이 지나면 예외가 발생합니다.                      | 1초 (1,000ms)              |

## 커넥션 풀 시나리오 설명

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-1.png)

`maximumPoolSize`가 10인 커넥션 풀을 가진 애플리케이션에서, 10명의 사용자가 각각 1초에 한 번씩 요청을 보낸다고 가정해보겠습니다. 각 요청은 약 1초가 소요됩니다. 아래 시나리오는 커넥션 풀의 상태를 각 단계별로 설명합니다.

### 커넥션 풀의 주요 필드 및 동작 시나리오 분석

* **maximumPoolSize**:
    - **설명**: 커넥션 풀에서 관리할 수 있는 최대 커넥션 수를 나타냅니다. 이 시나리오에서는 10개로 설정되어 있어, 동시에 최대 10개의 요청을 처리할 수 있습니다.
    - **이미지 설명**: 이미지에서 `totalConnections = 10`으로 설정되어 있으며, 이는 커넥션 풀에서 관리할 수 있는 총 커넥션이 10개임을 의미합니다.
* **activeConnections**:
    - **설명**: 현재 요청을 처리 중인 커넥션의 수입니다. 동시 요청이 10건 발생하면 `activeConnections`는 10이 됩니다. 더 이상 여유가 없는 상태에서 추가 요청이 들어오면 대기 상태가 됩니다.
    - **이미지 설명**: 첫 번째 그림에서 `activeConnections = 1`로, 한 개의 요청이 활성화된 상태입니다. 두 번째 그림에서는 `activeConnections = 5`로, 5명이 동시에 요청을 보내고 있습니다. 세 번째 그림에서는 `activeConnections = 10`으로, 모든 커넥션이 활성화되어 추가 요청을 처리할 수 없는 상태입니다.
* **idleConnections**:
    - **설명**: 유휴 상태로 대기 중인 커넥션의 수를 나타냅니다. 예를 들어, 첫 번째 요청이 처리될 때 `idleConnections`는 9개이며, 모든 커넥션이 활성화되면 `idleConnections`는 0이 됩니다.
    - **이미지 설명**: 첫 번째 그림에서 `idleConnections = 9`로, 9개의 커넥션이 대기 상태입니다. 두 번째 그림에서는 `idleConnections = 5`, 세 번째 그림에서는 `idleConnections = 0`으로, 모든 커넥션이 사용 중인 상태입니다.
* **totalConnections**:
    - **설명**: 커넥션 풀에서 관리하고 있는 총 커넥션 수로, `activeConnections`와 `idleConnections`의 합입니다. 이 값은 `maximumPoolSize` 내에서 유지되며, 동시 요청이 많을수록 `idleConnections`가 줄어듭니다.
    - **이미지 설명**: 세 개의 그림 모두 `totalConnections = 10`으로, 이는 커넥션 풀에서 관리하는 커넥션이 총 10개임을 나타냅니다.
* **threadsAwaitingConnection**:
    - **설명**: 커넥션이 모두 사용 중일 때 대기 중인 요청의 수를 나타냅니다. 예를 들어, 10명의 사용자가 모두 커넥션을 사용 중일 때 추가 요청이 발생하면, 그 요청은 대기 상태로 들어가 `threadsAwaitingConnection`이 증가합니다.
    - **이미지 설명**: 마지막 그림에서는 모든 커넥션이 사용 중이기 때문에, 추가 요청이 발생하면 대기 상태로 들어가게 됩니다.
* **connectionTimeout**:
    - **설명**: 대기 중인 요청이 커넥션을 얻기 위해 기다릴 수 있는 최대 시간을 나타냅니다. 예를 들어, `connectionTimeout`이 2초로 설정된 경우, 대기 중인 요청이 2초 내에 커넥션을 할당받지 못하면 요청은 실패하게 됩니다.
    - **이미지 설명**: 마지막 그림에서 모든 커넥션이 사용 중인 상태에서 추가 요청이 들어오면, `connectionTimeout` 내에 커넥션을 할당받지 못할 경우 해당 요청은 실패하게 됩니다.
* **validationTimeout**:
    - **설명**: 풀에서 커넥션을 빌려올 때 해당 커넥션이 유효한지 확인하는 시간입니다. 이 시간이 초과되면 해당 커넥션은 사용되지 않고 새로운 커넥션이 할당됩니다.
    - **이미지 설명**: 유휴 상태로 오래 있던 커넥션은 유효성 검사에서 실패할 수 있으며, 이 경우 새로운 커넥션이 할당됩니다. 이미지에서는 유휴 상태의 커넥션들이 대기 중인 상태를 보여줍니다.

## HikariCP 설정 및 상태 측정 샘플 코드

Spring Boot 애플리케이션에서 HikariCP를 사용하는 방법을 설명합니다. HikariCP는 Spring Boot에서 기본적으로 사용하는 커넥션 풀로, 설정을 통해 다양한 커넥션 관리 옵션을 제공합니다. 또한, HikariCP의 상태를 측정할 수 있는 방법을 추가하여 커넥션 풀의 효율적인 관리가 가능합니다.

### HikariCP Properties 설정

```yaml
spring:
    datasource:
        hikari:
            maximum-pool-size: 10           # 최대 커넥션 수
            minimum-idle: 10                # 최소 유휴 커넥션 수
            connection-timeout: 30000       # 커넥션을 가져올 때 대기할 최대 시간 (밀리초)
            max-lifetime: 1800000           # 커넥션이 유지될 최대 시간 (밀리초)
            idle-timeout: 600000            # 유휴 커넥션이 유지될 최대 시간 (밀리초)
            leak-detection-threshold: 2000  # 커넥션 리크를 감지할 기준 시간 (밀리초)
            pool-name: Sample-HikariPool    # 커넥션 풀 이름
            auto-commit: true               # 자동 커밋 여부
            validation-timeout: 5000        # 커넥션 유효성 검사를 위한 최대 시간 (밀리초)
            read-only: false                # 읽기 전용 모드 여부
            isolate-internal-queries: false # 내부 쿼리 격리 여부
            allow-pool-suspension: false    # 커넥션 풀 일시 정지 허용 여부
            initialization-fail-timeout: 1  # 초기화 실패 시 타임아웃 (밀리초)
```

### HikariCP 상태 측정 코드

```kotlin

@Service
class SampleService(
    private val dataSource: DataSource,
    private val memberRepository: MemberRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)!!

    @Transactional
    fun getMember(): Member {
        val member = memberRepository.findById(Random.nextInt(1, 101).toLong()).get()
        runBlocking { delay(1000) }
        val targetDataSource = dataSource.unwrap(HikariDataSource::class.java)
        val hikariDataSource = targetDataSource as HikariDataSource
        val hikariPoolMXBean = hikariDataSource.hikariPoolMXBean
        val hikariConfigMXBean = hikariDataSource.hikariConfigMXBean
        val log =
            """
            totalConnections : ${hikariPoolMXBean.totalConnections}
            activeConnections : ${hikariPoolMXBean.activeConnections}
            idleConnections : ${hikariPoolMXBean.idleConnections}
            threadsAwaitingConnection : ${hikariPoolMXBean.threadsAwaitingConnection}
            """.trimIndent()

        this.log.info(log)

        return member
    }
}
```

이 코드에서는 먼저 `DataSource`를 `HikariDataSource`로 변환한 후, `HikariPoolMXBean`과 `HikariConfigMXBean`을 사용하여 커넥션 풀의 상태를 확인합니다. 이를 통해 총 커넥션 수, 활성 커넥션 수, 유휴 커넥션 수, 그리고 대기 중인 스레드 수와 같은 정보를 가져옵니다. 또한, `runBlocking { delay(1000) }`을 사용하여 1초간의 지연을 추가함으로써, 실제 요청이 처리되는 동안 커넥션 풀의 상태를 보다 명확하게 모니터링할 수 있습니다. 마지막으로, 이러한 커넥션 풀의 상태를 로그로 출력하여 애플리케이션의 성능을 모니터링하고, 필요한 경우 성능을 조정할 수 있는 정보를 제공합니다.

## Connection Pool 측정

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-2.png)

애플리케이션에서 커넥션 풀을 사용할 때, 커넥션 풀의 상태를 지속적으로 모니터링하는 것은 매우 중요합니다. 아래와 같은 로그는 **10 TPS (초당 트랜잭션)**를 지속적으로 유지할 때 발생한 로그입니다. 이때 평균 응답 시간은 약 1,000ms로 나타나며, 이는 TPS 수준을 고려했을 때 시스템이 적정 수준에서 작동하고 있음을 보여줍니다.

- **로그 1**:
    - `totalConnections = 10`
    - `activeConnections = 7`
    - `idleConnections = 3`
    - `threadsAwaitingConnection = 0`

  이 상태는 **총 10개의 커넥션** 중 **7개가 활성화**되어 요청을 처리하고 있으며, **3개의 유휴 커넥션**이 대기 중인 상황입니다. 모든 요청이 원활하게 처리되고 있기 때문에, 대기 중인 스레드는 없습니다.

- **로그 2**:
    - `totalConnections = 10`
    - `activeConnections = 10`
    - `idleConnections = 0`
    - `threadsAwaitingConnection = 4`

  이 상태는 **모든 10개의 커넥션이 활성화**되어 요청을 처리 중이며, 더 이상 유휴 커넥션이 남아있지 않습니다. 이때 **4개의 추가 요청이 들어와 대기** 중입니다. `threadsAwaitingConnection` 값이 4로 증가한 이유는, 요청을 처리할 수 있는 유휴 커넥션이 없기 때문입니다.

### 커넥션 풀 동작 및 타임아웃 발생

커넥션 풀이 설정된 `maximumPoolSize`만큼 활성화된 경우, 그 이후에 들어오는 요청은 **대기 상태**에 들어가게 됩니다. 이때 **대기 시간이 길어질 수 있으며**, 이러한 대기 시간이 너무 길어지면 **타임아웃**이 발생할 수 있습니다. 타임아웃이 발생하는 주요 원인은 다음과 같습니다.

**connection-timeout**은 커넥션을 얻기 위해 스레드가 대기할 수 있는 최대 시간을 의미합니다. 예를 들어, `connection-timeout`이 30초로 설정되어 있다면, 커넥션 풀이 사용 가능한 커넥션을 30초 동안 제공하지 못할 경우 타임아웃이 발생하게 됩니다. 이 설정은 대기 중인 요청이 얼마 동안 기다릴 수 있는지를 제한합니다.

커넥션 풀은 **한정된 자원**을 효율적으로 관리하여 시스템의 안정성을 유지하는 좋은 방법입니다. 그러나, 만약 요청량이 설정된 `maximumPoolSize`를 초과하게 되면 대기 상태가 발생할 수 있습니다. 이러한 상황을 방지하기 위해 적절한 **타임아웃** 값을 설정하고, 필요에 따라 풀 크기를 조정하는 것이 중요합니다.

### connection-timeout 설정과 TPS 증가로 인한 오류 발생

```yaml
spring:
    datasource:
        hikari:
            maximum-pool-size: 10         # 최대 커넥션 수
            minimum-idle: 10              # 최소 유휴 커넥션 수
            connection-timeout: 250       # 커넥션을 가져올 때 대기할 최대 시간 (밀리초)
```

위 설정에서 `connection-timeout`을 250ms로 지정한 경우, **TPS**가 10을 초과하게 되면 **threadsAwaitingConnection**에 대기하는 시간이 250ms를 넘을 수 있습니다. 이 상황이 발생하면, 커넥션 풀은 설정된 대기 시간보다 오래 걸리기 때문에 타임아웃 오류가 발생하게 됩니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-3.png)

이미지에서와 같이, `RPS`(Request Per Second)가 10 이상일 때 커넥션 풀의 한계로 인해 대기 중인 요청이 발생하고, 그 대기 시간이 `250ms`를 초과하면 오류가 발생합니다. 이때 `Failures/s`가 증가하는 것을 확인할 수 있습니다. 이는 타임아웃 설정과 관련이 있으며, 커넥션 풀의 자원 한계와 처리량을 적절히 맞춰야 하는 이유를 보여줍니다.

**오류 메시지 예시**:

```
java.sql.SQLTransientConnectionException: Sample-HikariPool - Connection is not available, request timed out after 251ms.
	at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:696) ~[HikariCP-4.0.3.jar:na]
	at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197) 
```

이 오류는 대기 시간이 설정된 `connection-timeout`을 초과했음을 의미하며, 커넥션 풀이 추가 요청을 처리할 수 없다는 것을 나타냅니다. 이 문제를 해결하기 위해 여러 가지 방법을 고려할 수 있습니다.


### 쿼리 최적화

**쿼리 최적화**는 데이터베이스 성능 향상에 있어 중요한 단계입니다. 특히, 애플리케이션에서 `activeConnections`가 빠르게 응답하지 못하는 경우, 그 원인이 복잡한 쿼리 처리에 있다면 쿼리 최적화를 통해 성능을 크게 개선할 수 있습니다. 잘못 설계된 쿼리나 불필요하게 긴 실행 시간을 가진 쿼리는 시스템 전반의 성능에 영향을 미치고, 커넥션 풀의 자원을 오래 점유하게 되어 **대기 중인 요청**들이 지연되는 상황을 초래할 수 있습니다.

다음과 같은 방식으로 쿼리를 최적화함으로써 응답 속도를 개선할 수 있습니다:

```kotlin
@Transactional
fun getMember(): Member {
    val member = memberRepository.findById(Random.nextInt(1, 101).toLong()).get()
    // runBlocking { delay(1000) } 블록킹 코드 제거
    return member
}
```

위 코드에서 불필요한 블로킹 코드인 `runBlocking { delay(1000) }`를 제거함으로써 쿼리 실행 지연을 없앴습니다. 이와 같이, 쿼리 최적화는 단순히 코드 내의 블로킹 요소를 제거하는 것뿐만 아니라, **인덱스 추가**, **복잡한 조인 구조 단순화**, **캐싱** 등을 통해 데이터베이스에 대한 부하를 줄이는 방법도 포함됩니다. 이러한 최적화 작업을 통해 쿼리 실행 시간이 줄어들면 커넥션이 더 빨리 반환되고, **대기 중인 요청이 빠르게 처리**될 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-4.png)

쿼리 최적화를 통해 성능이 향상되면, **connection-timeout**이 250ms로 설정된 상황에서도 **RPS가 20**인 경우처럼 높은 요청 처리량에서도 타임아웃 오류가 발생하지 않게 됩니다. 이는 쿼리가 최적화되면 커넥션이 더 빠르게 반환되기 때문에, 커넥션 풀 내에서 더 많은 요청을 동시에 처리할 수 있기 때문입니다.

최적화된 쿼리는 커넥션 점유 시간을 줄여 **connection-timeout** 설정의 제한을 넘지 않도록 도와주며, 결과적으로 더 많은 트랜잭션을 안정적으로 처리할 수 있게 됩니다. 따라서, 쿼리 최적화는 애플리케이션 성능을 유지하고 향상시키는 중요한 방법입니다.

결론적으로, **쿼리 최적화**는 데이터베이스 성능 개선뿐만 아니라, 커넥션 풀 자원 관리에도 매우 중요한 역할을 합니다. 불필요한 대기 시간을 줄이고, 시스템이 고부하 상황에서도 원활하게 작동할 수 있도록 돕습니다.



### connection-timeout 시간 조정

- `connection-timeout`을 적절히 늘려 대기 중인 요청이 조금 더 오래 기다릴 수 있게 조정할 수 있습니다. 예를 들어, 250ms에서 500ms로 설정을 늘리면, 커넥션이 반환되기까지 더 많은 시간을 허용하게 되어 타임아웃 오류가 줄어들 수 있습니다. 다만, 너무 긴 시간으로 설정할 경우 다른 요청들이 더 오랜 시간 동안 대기해야 하므로 신중히 설정해야 합니다.

### maximum-pool-size 증가
- 애플리케이션에서 처리할 수 있는 동시 요청 수가 많아질 것으로 예상된다면, `maximum-pool-size`를 늘려 커넥션 풀에서 더 많은 커넥션을 생성하고 유지할 수 있도록 설정할 수 있습니다. 이를 통해 더 많은 요청을 동시에 처리할 수 있게 됩니다. 그러나, 무작정 풀 크기를 늘리는 것은 **자원 낭비**를 초래할 수 있기 때문에, 시스템의 메모리와 CPU 사용량을 고려한 후 적절히 조정해야 합니다.

`connection-timeout` 오류를 방지하기 위해서는 위와 같은 여러 가지 방법을 적절히 활용해야 합니다. **쿼리 최적화**는 가장 근본적인 성능 개선 방법이며, 이를 통해 커넥션 반환 시간을 단축할 수 있습니다. 또한, **timeout 설정**을 상황에 맞게 조정하거나 **maximum-pool-size**를 늘려 대기 시간을 줄일 수 있습니다. 다만, 이러한 설정 값들은 무작정 크게 늘린다고 좋은 해결책이 되는 것은 아니며, **애플리케이션의 특성**과 **시스템 자원**을 고려하여 최적의 값을 찾아야 합니다.

## 결론

HikariCP를 활용한 커넥션 풀은 애플리케이션의 성능을 극대화하는 중요한 요소입니다. **커넥션 풀**을 통해 데이터베이스와의 연결을 효율적으로 관리하고, 미리 준비된 커넥션을 재사용함으로써 응답 시간을 줄일 수 있습니다. 그러나, **maximumPoolSize**와 **connection-timeout**과 같은 설정을 적절히 조정하지 않으면, TPS(초당 트랜잭션)가 증가할 때 커넥션 풀에서 대기 시간이 발생하고, 결국 **타임아웃 오류**가 발생할 수 있습니다.

이러한 오류를 방지하기 위해서는 다음과 같은 사항을 고려해야 합니다.

1. **커넥션 풀의 크기**를 애플리케이션의 트래픽에 맞게 적절히 조정합니다. 트래픽이 증가할 경우, 풀의 크기도 이에 맞춰 확장할 필요가 있습니다.
2. **connection-timeout**을 적절하게 설정하여 대기 중인 요청이 너무 오래 걸리지 않도록 관리합니다.
3. **TPS**(초당 요청 처리량)를 지속적으로 모니터링하고, 필요시 **HikariCP**의 설정 값을 조정하여 성능을 최적화합니다.

적절한 커넥션 풀 설정과 타임아웃 관리로 애플리케이션의 안정성과 성능을 보장할 수 있으며, 대기 시간과 오류를 최소화하여 안정적인 시스템 운영을 가능하게 할 수 있습니다.

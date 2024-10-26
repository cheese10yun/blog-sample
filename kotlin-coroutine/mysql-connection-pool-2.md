# HikariCP로 MySQL Connection Pool 최적화하기

Spring Boot는 기본적으로 HikariCP를 내장된 커넥션 풀로 지원하며, 이를 통해 데이터베이스 연결을 효율적으로 관리할 수 있습니다. 이번 포스팅에서는 Spring Boot 환경에서 HikariCP의 설정을 최적화하여 TPS 변화에 유연하게 대응하는 방법을 알아보겠습니다.

## HikariCP 설정 예시

Spring Boot에서 application.yml 또는 application.properties 파일을 통해 HikariCP 설정을 정의할 수 있습니다. 이번 예시에서는 다음과 같은 설정을 적용하였습니다.

```yaml
spring:
    datasource:
        hikari:
            minimum-idle: 10
            maximum-pool-size: 50
            idle-timeout: 30000
            connection-timeout: 20000
            pool-name: "HikariCP"
```

- **minimum-idle**: 최소 유휴 커넥션 수입니다. 초기 설정 시 최소한의 커넥션(여기서는 10개)만 유지하여, TPS가 낮을 때 리소스를 절약할 수 있습니다.
- **maximum-pool-size**: 커넥션 풀의 최대 크기입니다. TPS가 높아질 때 최대 50개의 커넥션까지 생성하여 요청을 처리할 수 있게 설정합니다.
- **idle-timeout**: 지정된 시간(밀리초) 동안 유휴 상태인 커넥션이 있을 경우 풀에서 제거합니다. 트래픽이 낮아질 때 자동으로 풀 크기를 줄이는 데 기여합니다.
- **connection-timeout**: 커넥션을 얻기 위해 대기하는 최대 시간입니다. 이 시간 내에 커넥션을 확보하지 못하면 예외가 발생합니다.

## TPS 변화에 따른 커넥션 풀의 동작

1. **TPS가 낮은 경우**: Spring Boot 애플리케이션이 유휴 상태이거나 트래픽이 적은 경우, HikariCP는 최소 커넥션(minimum-idle)만 유지하여 리소스 사용을 최적화합니다.
2. **TPS가 높아질 경우**: TPS가 증가하여 커넥션이 필요한 상황이 되면, HikariCP는 최대 커넥션(maximum-pool-size)까지 확장하여 대량의 요청을 처리할 수 있게 합니다. 이를 통해 성능 저하 없이 안정적으로 트래픽을 소화할 수 있습니다.
3. **TPS가 다시 낮아지는 경우**: TPS가 다시 낮아지면 HikariCP는 `idle-timeout`에 따라 불필요한 커넥션을 풀에서 제거하고, `minimum-idle`만 유지하여 리소스를 절약합니다.

이 케이스는 MySQL Connection Pool에서 `minimum-idle`과 `maximum-pool-size`를 동일하게 설정한 상황에서, TPS가 200대에 도달할 때 발생하는 성능 문제를 다루고 있습니다. 그래프와 로그를 바탕으로 아래와 같이 분석할 수 있습니다.

## TPS가 높아지는 상황에서의 커넥션 풀 동작 분석

Spring Boot 애플리케이션에서 TPS가 높아질 때, HikariCP의 커넥션 풀이 어떻게 반응하고 성능에 어떤 영향을 미치는지 살펴보겠습니다. 이 테스트는 `minimum-idle: 10`과 `maximum-pool-size: 10` 설정을 사용해, 커넥션 풀의 확장성과 한계점을 확인하는 데 중점을 두었습니다.

애플리케이션은 지속적으로 증가하는 사용자 요청을 처리하며, TPS가 증가함에 따라 커넥션 풀이 최대에 도달하는 시점에서 성능 지연과 요청 실패가 발생하는 과정을 시각적으로 분석했습니다.

### 상황 설명

다음 코드는 Spring Boot와 Kotlin 환경에서 설정된 컨트롤러와 서비스 로직입니다. 컨트롤러에서는 `SampleService`의 `getMember()` 메서드를 호출하며, 이 메서드는 1~100 사이의 랜덤 ID로 `Member` 엔티티를 PK를 기반으로 조회한 후, 1초의 지연 시간을 둔 뒤 커넥션 풀의 현재 상태를 로깅합니다.

```kotlin
@RestController
@RequestMapping
class SampleController(
    private val SampleService: SampleService
) {
    
    @GetMapping("/api/v1/members")
    fun sample(): Member {
        // 1 ~ 100 사이의 랜덤으로 member 조회
        return SampleService.getMember()
    }
}
```

```kotlin
@Service
class SampleService(
    private val dataSource: DataSource,
    private val memberRepository: MemberRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)!!

    @Transactional
    fun getMember(): Member {
        val findById = memberRepository.findById(Random.nextInt(1, 100).toLong()).get()
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
            maxLifetime : ${hikariConfigMXBean.maxLifetime}
            maximumPoolSize : ${hikariConfigMXBean.maximumPoolSize}
            minimumIdle : ${hikariConfigMXBean.minimumIdle}
            connectionTimeout : ${hikariConfigMXBean.connectionTimeout}
            validationTimeout : ${hikariConfigMXBean.validationTimeout}
            idleTimeout : ${hikariConfigMXBean.idleTimeout}
            """.trimIndent()
        this.log.info(log)
        return findById
    }
}
```

이 코드는 지연을 위해 1초 동안 대기한 후, HikariCP 커넥션 풀의 상태를 로깅하여 현재 커넥션 풀 상황을 모니터링할 수 있게 합니다.

### 성능 테스트 결과 (위 이미지 설명)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-1-1.png)

위 이미지는 커넥션 풀 설정이 **minimum-idle: 10, maximum-pool-size: 10**으로 설정된 상황에서, TPS가 증가함에 따라 성능이 어떻게 변화하는지를 시각화한 결과입니다.

- **Total Requests per Second**:
  - 이 그래프는 초당 요청 처리량(RPS, 초록색 라인)과 실패한 요청(Failures, 빨간색 라인)을 보여줍니다.
  - TPS가 50에서 200까지 점진적으로 증가하면서도, 실패한 요청은 발생하지 않았습니다. 이는 시스템이 최대 커넥션 풀이 가득 찼을 때도 요청을 대기시키며 처리하는 것을 의미합니다.
- **Response Times**:
  - 응답 시간 그래프에서는 **95th 퍼센타일**(보라색 라인)이 급격히 상승하는 순간이 보입니다. 이는 TPS가 200대에 도달했을 때 응답 시간이 길어지는 현상을 나타냅니다. 이는 커넥션 풀이 가득 차서 새로운 요청이 대기 상태로 전환되었기 때문입니다.
  - 그 후 트래픽이 유지되는 동안 응답 시간이 다시 안정화되는 모습이 보이는데, 이는 스레드 대기 시간이 감소하면서 시스템이 다시 원활히 작동하기 시작한 것을 보여줍니다.
- **Number of Users**:
  - 사용자의 수는 시간에 따라 지속적으로 증가하며, 시스템의 부하를 점점 더 많이 가하는 상황을 묘사하고 있습니다. 사용자가 100명 이상일 때 커넥션 풀의 한계에 도달하면서 성능 저하가 발생하기 시작합니다.

### 로그 분석

- **activeConnections**: 10 - 현재 활성 상태인 모든 커넥션이 사용 중입니다.
- **idleConnections**: 0 - 유휴 상태의 커넥션은 없습니다.
- **threadsAwaitingConnection**: 84 - 84개의 스레드가 커넥션을 기다리고 있습니다.
- **maxLifetime**: 1800000 (밀리초) - 커넥션의 최대 수명입니다.
- **maximumPoolSize**: 10 - 최대 커넥션 풀 크기가 10으로 설정되어 있습니다.
- **minimumIdle**: 10 - 최소 유휴 커넥션이 10으로 설정되어 있습니다.
- **connectionTimeout**: 30000 (밀리초) - 커넥션을 얻기 위해 대기할 수 있는 최대 시간입니다.
- **validationTimeout**: 5000 (밀리초) - 커넥션 유효성 검사를 위한 시간입니다.
- **idleTimeout**: 600000 (밀리초) - 유휴 커넥션을 유지하는 최대 시간입니다.

이 로그는 커넥션 풀이 한계에 도달하여 더 이상 커넥션을 확장할 수 없고, 여러 스레드가 커넥션을 기다리면서 성능 저하가 발생하고 있음을 보여줍니다.

### 문제 원인

1. **커넥션 풀 크기 제한**: 현재 `maximum-pool-size`가 10으로 설정되어 있어 TPS 200대를 감당하기에는 커넥션 풀의 크기가 부족합니다. 모든 커넥션이 이미 사용 중이기 때문에 추가적인 요청이 들어오면 대기하게 되고, 그로 인해 응답 시간이 증가합니다.
2. **스레드 대기**: `threadsAwaitingConnection`이 71까지 증가한 것은 커넥션 풀이 더 이상 확장되지 않으면서 스레드가 커넥션을 기다리는 상황을 의미합니다. 이는 TPS가 증가할 때 시스템이 즉각적으로 대응하지 못하고 성능 저하를 일으키는 주요 원인 중 하나입니다.

### 해결 방안

1. **maximum-pool-size 증가**: TPS가 증가할 때 커넥션 풀이 충분히 확장할 수 있도록 `maximum-pool-size`를 더 큰 값으로 설정하는 것이 필요합니다. 예를 들어, 50 또는 그 이상의 값으로 설정하여 높은 TPS를 처리할 수 있도록 해야 합니다.
2. **동적 커넥션 관리**: HikariCP의 기본 동작 원리에 맞춰 `minimum-idle`과 `maximum-pool-size`를 유연하게 설정하는 것이 중요합니다. 커넥션 풀이 트래픽에 맞춰 확장되도록 하고, TPS가 감소할 때는 리소스를 절약할 수 있도록 설정해야 합니다.
3. **모니터링 및 튜닝**: 모니터링을 통해 커넥션 풀의 동작을 지속적으로 확인하고, 트래픽 패턴에 따라 적절하게 설정을 조정하는 것이 중요합니다.

이와 같은 방식으로, 트래픽 변화에 대응할 수 있는 유연한 커넥션 풀 관리가 필요합니다.
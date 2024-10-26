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
    - TPS가 점진적으로 증가하여 초당 12 요청 수준에 도달했을 때, 실패한 요청이 발생하기 시작했습니다. 이는 커넥션 풀이 최대 용량인 10에 도달하여 더 이상 추가 요청을 처리하지 못하는 상황을 나타냅니다.
    - 이후 TPS는 유지되지만, 실패한 요청이 지속적으로 발생하면서 커넥션 풀의 제한에 따른 성능 저하가 명확히 드러납니다. 이후 TPS는 응답 지연으로 인해 더 이상 올라가지 않습니다.
- **Response Times**:
    - 응답 시간 그래프에서는 **50th 퍼센타일**(주황색 라인)과 **95th 퍼센타일**(보라색 라인)의 응답 시간이 시간이 지남에 따라 증가하는 모습이 보입니다.
    - 특히 TPS가 증가함에 따라 95th 퍼센타일 응답 시간은 약 20,000ms 이상으로 치솟아, 사용자 요청이 큰 지연을 겪고 있음을 나타냅니다.
    - 이는 커넥션 풀이 가득 차서 새로운 요청이 대기 상태로 전환되었기 때문이며, 트래픽 증가와 함께 시스템의 성능 한계에 도달했음을 보여줍니다.
- **Number of Users**:
    - 사용자의 수가 점진적으로 증가하며 시스템에 부하를 가하고 있습니다. 사용자가 약 300명 이상일 때부터 시스템은 커넥션 풀이 한계에 도달하여, 그 이후로는 성능 저하가 본격적으로 발생합니다.
    - 커넥션 풀 크기를 초과하는 사용자 요청은 실패하거나 긴 대기 시간을 초래하게 되며, 이는 응답 시간 증가와 TPS 유지의 원인이 됩니다.

이 이미지에서는 커넥션 풀이 최대 용량에 도달함에 따라, 시스템이 추가적인 요청을 감당하지 못하고 지연 시간과 실패율이 증가하는 과정을 시각적으로 확인할 수 있습니다.

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

1. **커넥션 풀 크기 제한**: 현재 `maximum-pool-size`가 10으로 설정되어 있어 초당 12개 이상의 요청(TPS)을 처리하기에는 커넥션 풀 크기가 부족합니다. 모든 커넥션이 이미 사용 중이기 때문에, 추가적인 요청이 들어오면 커넥션을 기다리게 되고, 이로 인해 요청 실패가 발생하거나 응답 시간이 길어집니다.
2. **스레드 대기**: 커넥션 풀이 최대 용량에 도달하면서 `threadsAwaitingConnection` 수가 증가하게 됩니다. 이는 커넥션을 얻지 못한 요청이 대기 상태로 전환되는 상황을 나타내며, TPS가 증가할 때 시스템이 추가 요청을 즉각적으로 처리하지 못하고 성능 저하를 초래하는 주요 원인이 됩니다.
3. **connectionTimeout 설정**: 현재 `connectionTimeout`이 30000밀리초(30초)로 설정되어 있어, 커넥션을 기다리는 요청은 최대 30초까지 대기할 수 있습니다. 그러나 이 대기 시간이 길어질수록 전체 응답 시간이 증가하게 되며, 대기 중인 요청이 많아지면 TPS가 상승하기 어려워지고 응답 지연으로 인한 성능 저하가 발생할 수 있습니다.

이러한 문제들은 TPS가 높아질수록 커넥션 풀의 제한으로 인해 전체적인 성능 저하가 발생하게 되는 이유입니다.

## 해결 방안

1. **maximum-pool-size 증가**: 현재의 TPS 수요를 충족하기 위해 `maximum-pool-size` 값을 늘려야 합니다. 예를 들어, 50 이상으로 설정하여 커넥션 풀이 더 많은 요청을 처리할 수 있도록 하면, 요청 대기 시간과 실패를 줄일 수 있습니다.
2. **동적 커넥션 관리**: HikariCP의 특성을 활용해 `minimum-idle`과 `maximum-pool-size`를 적절히 조정하여 트래픽 변화에 유연하게 대응할 수 있도록 합니다. TPS가 높아질 때는 커넥션 풀이 자동으로 확장되도록 하고, TPS가 감소할 때는 최소한의 커넥션만 유지해 리소스를 절약하도록 설정하는 것이 좋습니다.
3. **모니터링 및 지속적인 튜닝**: 커넥션 풀의 상태를 지속적으로 모니터링하여, 트래픽 패턴에 맞게 적절히 튜닝하는 것이 필요합니다. 정기적인 모니터링을 통해 TPS와 응답 시간 변화를 관찰하고, 필요에 따라 `maximum-pool-size`, `connectionTimeout` 등의 설정을 조정하여 최적의 성능을 유지할 수 있습니다.

이러한 방안들은 트래픽 변동에 따라 유연하게 커넥션 풀을 관리하고, 시스템 성능을 최적화하는 데 도움이 됩니다.

### 성능 테스트 결과 분석: maximum-pool-size를 200으로 조정한 경우

```yaml
spring:
    datasource:
        hikari:
            maximum-pool-size: 200           # 최대 커넥션 수
            minimum-idle: 10                 # 최소 유휴 커넥션 수
```

위와 같이 `maximum-pool-size`를 200, `minimum-idle`을 10으로 설정하여 테스트를 진행한 결과, 커넥션 풀이 충분히 확장 가능해지면서 시스템 성능이 크게 개선되었습니다. 주요 개선 사항은 다음과 같습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-1-2.png)

- **Total Requests per Second (RPS)**:
    - RPS가 점진적으로 증가하여 높은 TPS를 안정적으로 처리할 수 있게 되었습니다. 초당 요청 처리량이 약 150까지 증가했음에도 불구하고, 실패한 요청(Failures/s)은 발생하지 않았습니다.
    - 이는 커넥션 풀이 충분히 확장되어, 모든 요청이 처리되는 동안 커넥션 부족으로 인한 대기 시간이 발생하지 않았음을 의미합니다.
- **Response Times**:
    - 응답 시간 그래프에서 50th 및 95th 퍼센타일 응답 시간이 비교적 안정적인 수준을 유지하고 있습니다.
    - 95th 퍼센타일 응답 시간은 약 3,000ms 이하로, 50th 퍼센타일은 약 1,000ms 내외로 유지되었습니다. 이는 고TPS 상황에서도 일관된 응답 속도를 제공할 수 있음을 보여줍니다.
    - 이전 설정에서 발생했던 응답 시간의 급격한 증가가 해소되어, 사용자 경험이 크게 개선되었습니다.
- **Failures/s 비율**:
    - 요청 실패율이 0으로 유지되었습니다. `maximum-pool-size`를 200으로 설정한 덕분에, `connectionTimeout`으로 인해 대기 상태에서 실패하는 요청이 없었습니다.
    - 이로써 고TPS 상황에서도 안정적인 서비스가 가능해졌으며, 대량의 동시 요청을 처리하는 데 적합한 환경이 조성되었습니다.

### 로그 분석

- **totalConnections**: 152 - 현재 총 152개의 커넥션이 생성되었습니다.
- **activeConnections**: 152 - 모든 커넥션이 활성 상태로 사용 중입니다.
- **idleConnections**: 0 - 유휴 상태의 커넥션은 없습니다.
- **threadsAwaitingConnection**: 48 - 48개의 스레드가 커넥션을 기다리고 있습니다.
- **maximumPoolSize**: 200 - 최대 커넥션 풀 크기가 200으로 설정되었습니다.
- **minimumIdle**: 200 - 최소 유휴 커넥션이 200으로 설정되어 있어, 초기에 모든 커넥션이 생성된 상태입니다.

이 설정을 통해 커넥션 풀은 트래픽이 적을 때에는 최소한의 자원만 사용하고, 트래픽이 증가할 때에는 최대 200개의 커넥션까지 확장하여 요청을 처리할 수 있습니다. 로그에서 볼 수 있듯이 TPS가 높은 상황에서도 커넥션 풀이 충분히 확장되었고, 전체적인 시스템 성능에는 큰 영향을 미치지 않았습니다. 이를 통해 시스템은 높은 TPS 환경에서도 안정적이고 일관된 성능을 제공할 수 있음을 확인할 수 있습니다.

## 성능 테스트 결과 분석: TPS 감소 상황에서의 커넥션 풀 동작

아래 설정으로 TPS가 낮아진 상황에서 성능 테스트를 진행했습니다.

```yaml
spring:
    datasource:
        hikari:
            maximum-pool-size: 200           # 최대 커넥션 수
            minimum-idle: 10                 # 최소 유휴 커넥션 수
```

### 테스트 결과 분석

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-1-3.png)

- **Total Requests per Second (RPS)**:
    - TPS가 낮아지며, 초당 요청 처리량이 약 5 수준으로 안정화되었습니다.
    - 요청 실패(Failures/s)가 발생하지 않았으며, 모든 요청이 성공적으로 처리되었습니다.
    - 이는 트래픽이 줄어들면서 커넥션 풀이 유휴 상태로 돌아가고 있음을 의미합니다.
- **Response Times**:
    - 50th 및 95th 퍼센타일 응답 시간 모두 약 1,000ms 내외로 일정하게 유지되고 있습니다.
    - 응답 시간의 변동이 크지 않고 안정적인 수준을 보여, TPS가 낮아진 상황에서도 일관된 성능을 제공하고 있습니다.
- **Number of Users**:
    - 테스트에서 사용자 수가 10명으로 일정하게 유지되고 있으며, TPS가 낮은 상태로 안정화되었습니다.

### 로그 분석

- **totalConnections**: 17 - 현재 총 17개의 커넥션이 생성되었습니다. 이전에 높은 트래픽으로 인해 확장되었던 커넥션 풀의 크기가 트래픽 감소에 따라 줄어들고 있습니다.
- **activeConnections**: 7 - 활성 상태의 커넥션이 7개로 줄어들었습니다.
- **idleConnections**: 10 - 최소 유휴 커넥션 설정(minimumIdle)이 10이므로, 트래픽이 낮을 때 10개의 유휴 커넥션을 유지합니다.
- **threadsAwaitingConnection**: 0 - 커넥션을 기다리는 스레드가 없으며, 요청이 대기 없이 처리되고 있습니다.
- **maximumPoolSize**: 200 - 최대 커넥션 풀 크기가 200으로 설정되어 있지만, 현재 TPS 수준에서는 모든 커넥션을 사용할 필요가 없어 크기가 줄어들었습니다.
- **minimumIdle**: 10 - 최소 유휴 커넥션이 10으로 유지되어 트래픽이 적을 때 불필요한 커넥션 자원을 절약합니다.

이 테스트 결과는 TPS가 줄어들면 `totalConnections`도 줄어드는 것을 보여줍니다. `minimum-idle` 설정 덕분에 커넥션 풀이 10개의 유휴 커넥션을 유지하고, 나머지 불필요한 커넥션은 자동으로 해제되어 리소스를 효율적으로 관리합니다. 이를 통해 시스템은 트래픽 변화에 따라 유연하게 대응할 수 있으며, 불필요한 자원 낭비를 최소화할 수 있습니다.



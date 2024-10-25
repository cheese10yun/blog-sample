# Spring Boot에서 HikariCP로 MySQL Connection Pool 최적화하기

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

### 상황 설명

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-coroutine/images/mysql-connection-pool-1-1.png)

1. **minimum-idle: 10, maximum-pool-size: 10 설정**:
   이 경우, 커넥션 풀의 크기가 고정되어 있으며, 최소한의 커넥션을 유지하고 최대 10개의 커넥션까지만 사용 가능합니다. `idleConnections`는 0이고, `activeConnections`는 10으로, 풀에 있는 모든 커넥션이 활성화된 상태입니다.
2. **TPS 200대에 응답 지연 발생**:
   트래픽이 증가하여 TPS가 200에 도달했을 때, 커넥션 풀이 한계에 도달하여 더 이상 커넥션을 할당할 수 없기 때문에 성능 저하가 발생합니다. 이로 인해 `threadsAwaitingConnection`이 71개로 증가하며, 이는 커넥션을 기다리고 있는 스레드의 수를 의미합니다. 커넥션 풀이 부족하여 대기 시간이 길어져 응답 속도가 늦어지는 것을 나타냅니다.

### 로그 분석

로그에서 나타난 주요 지표는 다음과 같습니다:

- **totalConnections: 10**: 총 10개의 커넥션이 생성되어 있음.
- **activeConnections: 10**: 모든 커넥션이 현재 활성 상태로 사용 중임.
- **idleConnections: 0**: 유휴 상태인 커넥션은 없음.
- **threadsAwaitingConnection: 71**: 71개의 스레드가 커넥션을 기다리고 있음.
- **maximumPoolSize: 10**: 커넥션 풀의 최대 크기가 10으로 설정되어 있음.
- **minimumIdle: 10**: 최소 유휴 커넥션이 10으로 설정되어 있음.

### 문제 원인

1. **커넥션 풀 크기 제한**: 현재 `maximum-pool-size`가 10으로 설정되어 있어 TPS 200대를 감당하기에는 커넥션 풀의 크기가 부족합니다. 모든 커넥션이 이미 사용 중이기 때문에 추가적인 요청이 들어오면 대기하게 되고, 그로 인해 응답 시간이 증가합니다.
2. **스레드 대기**: `threadsAwaitingConnection`이 71까지 증가한 것은 커넥션 풀이 더 이상 확장되지 않으면서 스레드가 커넥션을 기다리는 상황을 의미합니다. 이는 TPS가 증가할 때 시스템이 즉각적으로 대응하지 못하고 성능 저하를 일으키는 주요 원인 중 하나입니다.

### 해결 방안

1. **maximum-pool-size 증가**: TPS가 증가할 때 커넥션 풀이 충분히 확장할 수 있도록 `maximum-pool-size`를 더 큰 값으로 설정하는 것이 필요합니다. 예를 들어, 50 또는 그 이상의 값으로 설정하여 높은 TPS를 처리할 수 있도록 해야 합니다.
2. **동적 커넥션 관리**: HikariCP의 기본 동작 원리에 맞춰 `minimum-idle`과 `maximum-pool-size`를 유연하게 설정하는 것이 중요합니다. 커넥션 풀이 트래픽에 맞춰 확장되도록 하고, TPS가 감소할 때는 리소스를 절약할 수 있도록 설정해야 합니다.
3. **모니터링 및 튜닝**: 모니터링을 통해 커넥션 풀의 동작을 지속적으로 확인하고, 트래픽 패턴에 따라 적절하게 설정을 조정하는 것이 중요합니다.

이와 같은 방식으로, 트래픽 변화에 대응할 수 있는 유연한 커넥션 풀 관리가 필요합니다.
### MySQL Connection Pool이란?

애플리케이션에서 MySQL 데이터베이스와의 효율적인 연결을 위해 **커넥션 풀(Connection Pool)**을 사용합니다. 커넥션 풀은 미리 일정한 수의 데이터베이스 연결을 생성해 두고, 애플리케이션이 필요할 때마다 이 연결들을 재사용함으로써 성능을 향상시키는 기법입니다. 이 방식은 데이터베이스와의 연결을 매번 새로 생성하는 대신 이미 준비된 연결을 재사용하여 애플리케이션의 응답 시간을 줄이는 데 매우 유용합니다.

### Java JDBC와 HikariCP

Java 애플리케이션에서 가장 널리 사용되는 커넥션 풀 중 하나는 **HikariCP**입니다. HikariCP는 가볍고 빠른 커넥션 풀로, 대규모 트래픽이 발생하는 상황에서도 효율적인 연결 관리를 지원합니다. HikariCP는 **HikariPoolMXBean**과 **HikariConfigMXBean**이라는 JMX(Java Management Extensions)를 통해 커넥션 풀의 상태 및 설정을 관리할 수 있습니다.

### HikariPoolMXBean과 HikariConfigMXBean 필드 설명

| 필드                            | 설명                                                                     |
|-------------------------------|------------------------------------------------------------------------|
| **totalConnections**          | 커넥션 풀에서 총 생성된 커넥션의 수입니다. `activeConnections`와 `idleConnections`의 합입니다. |
| **activeConnections**         | 현재 사용 중인, 즉 애플리케이션이 데이터를 처리하는 데 사용하는 연결의 수입니다.                         |
| **idleConnections**           | 현재 유휴 상태로 대기 중인 연결의 수입니다. 애플리케이션이 요청하면 사용될 수 있습니다.                     |
| **threadsAwaitingConnection** | 사용 가능한 커넥션이 없어 대기 중인 요청 스레드의 수입니다.                                     |
| **maxLifetime**               | 커넥션이 유지될 수 있는 최대 시간입니다. 이 시간이 지나면 커넥션은 재사용되지 않고 폐기됩니다.                 |
| **maximumPoolSize**           | 커넥션 풀에서 허용되는 최대 연결 수입니다. 최대 이 수치만큼 커넥션을 생성하고 유지합니다.                    |
| **connectionTimeout**         | 커넥션을 가져오기 위해 스레드가 대기할 수 있는 최대 시간입니다. 이 시간이 지나면 예외가 발생합니다.              |
| **validationTimeout**         | 커넥션이 유효한지 확인하는 데 사용할 수 있는 최대 시간입니다. 유효성을 검사하는 데 사용됩니다.                 |
| **idleTimeout**               | 유휴 상태의 커넥션이 유지될 수 있는 최대 시간입니다. 이 시간이 지나면 유휴 커넥션은 제거됩니다.                |

### 커넥션 풀에서의 시나리오

**상황**: 커넥션 풀의 `maximumPoolSize`가 10인 경우를 가정해보겠습니다. 10명의 사용자가 각각 1초에 한 번씩 요청을 보내고, 각 요청은 약 1초의 시간이 소요됩니다.

1. **첫 번째 요청**: 사용자가 1초 동안 요청을 보냅니다. 커넥션 풀에서 1개의 연결을 할당하여 데이터를 처리하고, 1초 후에 반환합니다. 이때, `activeConnections`는 1개, `idleConnections`는 9개입니다.

2. **동시 요청 (10명)**: 10명의 사용자가 동시에 요청을 보냅니다. `activeConnections`는 10개가 되고, `idleConnections`는 0개가 됩니다. 이때, 10개의 커넥션이 모두 사용 중입니다.

3. **추가 요청 발생**: 만약 10명의 사용자가 동시에 1초에 1번씩 요청을 보내는 상황에서 11번째 요청이 들어온다면, 커넥션 풀이 꽉 차 있기 때문에 `threadsAwaitingConnection`에 해당 요청이 대기하게 됩니다. 이 대기 중인 요청의 수는 `threadsAwaitingConnection`으로 관리됩니다. `totalConnections`는 여전히 10 (`activeConnections + idleConnections`)으로 유지됩니다.

4. **1초 후 첫 번째 요청 처리 완료**: 1초 후 첫 번째 요청이 완료되면, 사용된 커넥션은 다시 유휴 상태로 돌아갑니다. 이제 `activeConnections`는 9개, `idleConnections`는 1개가 됩니다. 이때, 대기 중이던 11번째 요청이 처리되기 위해 풀에서 남은 유휴 커넥션을 할당받습니다.

### 각 필드에 대한 시나리오 설명

- **maximumPoolSize**: 커넥션 풀에서 생성할 수 있는 최대 커넥션 수로, 이 시나리오에서는 10개입니다. 즉, 동시에 10개의 요청을 처리할 수 있습니다. 10개 이상의 요청이 들어오면 추가 요청은 대기 상태에 들어갑니다.

- **activeConnections**: 현재 처리 중인 요청에 할당된 커넥션 수를 나타냅니다. 위 시나리오에서 동시 요청 10건이 발생하면 `activeConnections`는 10개가 됩니다.

- **idleConnections**: 요청이 없는 동안 유휴 상태로 대기 중인 커넥션 수입니다. 위 시나리오에서 첫 번째 요청이 처리될 때 `idleConnections`는 9개이며, 10개 요청이 모두 발생하면 `idleConnections`는 0이 됩니다.

- **totalConnections**: `activeConnections`와 `idleConnections`의 합으로, 총 커넥션 풀에서 관리하는 커넥션 수를 나타냅니다. 이 수는 `maximumPoolSize` 이상으로 증가하지 않습니다.

### 추가적으로 고려할 필드: `connectionTimeout`과 `validationTimeout`

- **connectionTimeout**: 만약 대기 중인 요청(즉, `threadsAwaitingConnection`에 있는 요청)이 `connectionTimeout` 내에 커넥션을 할당받지 못하면 예외가 발생합니다. 예를 들어, `connectionTimeout`이 2초로 설정되어 있고 11번째 요청이 들어왔을 때 2초 동안 커넥션을 할당받지 못하면, 요청은 실패하게 됩니다.

- **validationTimeout**: 풀에서 커넥션을 빌려올 때 해당 커넥션이 유효한지 확인하는 시간입니다. 만약 유효성 검사를 통과하지 못하거나 `validationTimeout`을 초과하면, 해당 커넥션은 사용되지 않고 새로운 커넥션이 할당됩니다.

---

### 결론

커넥션 풀을 적절하게 구성하면 애플리케이션의 성능을 크게 향상시킬 수 있습니다. `HikariCP`의 다양한 설정을 통해 애플리케이션의 트래픽 양에 맞춰 커넥션 풀을 최적화할 수 있으며, 동시에 비효율적인 커넥션 관리로 인한 자원 낭비를 줄일 수 있습니다.
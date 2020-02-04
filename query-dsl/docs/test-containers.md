# TestContainer

* 테스트에서 도커 컨테이너를 실행할 수 있는 라이브러리.
  * https://www.testcontainers.org/
  * 테스트 실행시 DB를 설정하거나 별도의 프로그램 또는 스크립트를 실행할 필요 없다.
  * 보다 Production에 가까운 테스트를 만들 수 있다.
  * 테스트가 느려진다.

## 의존성 추가

```gradle
dependencies {
    testImplementation("org.testcontainers:junit-jupiter:1.12.5")
    testImplementation("org.testcontainers:mysql:1.12.5")
}
```

## Getting Started

```kotlin
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@ActiveProfiles("test")
@Testcontainers
abstract class SpringBootTestSupport {
    
    companion object {
        @JvmStatic
        @Container
        val container = PostgreSQLContainer<Nothing>()
            .apply {
                withDatabaseName("querydsl")
            }
    }
}
```
* `@Testcontainers`, `@Container` 어노테이션을 이용해서 편리하게 컨테이너 환경을 만들 수 있음
*  컨테이너 객체를 `static`으로 생성하면 컨테이너 중복 생성을 방지할 수 있다.
  * `Junut`에서는 테스트 메서드 실행 마다 테스트 인스턴스를 새로 만들기 때문에 테스트 메서드에서 공유할 수 있는 `static`으로 만들어여한다.
  * `static`으로 만들지 않으면 테스트 메서드 실행마다 해당 컨테이너를 새롭게 만들기 때문에 테스트 속도가 매우 느림, 테스트 인스턴스 cycle을 변경해서 해결 할수 있음, ex-> `@TestInstance(Lifecycle.PER_CLASS`
  
  
```
02:37:48.693 [Test worker] DEBUG org.testcontainers.images.AbstractImagePullPolicy - Using locally available and not pulling image: postgres:9.6.12
02:37:48.693 [Test worker] DEBUG 🐳 [postgres:9.6.12] - Starting container: postgres:9.6.12
02:37:48.694 [Test worker] DEBUG 🐳 [postgres:9.6.12] - Trying to start container: postgres:9.6.12
02:37:48.694 [Test worker] DEBUG 🐳 [postgres:9.6.12] - Trying to start container: postgres:9.6.12 (attempt 1/1)
02:37:48.694 [Test worker] DEBUG 🐳 [postgres:9.6.12] - Starting container: postgres:9.6.12
02:37:48.695 [Test worker] INFO 🐳 [postgres:9.6.12] - Creating container for image: postgres:9.6.12
``` 
* docker container 기반으로 정상 동작하는것을 확인 할 수 있음

## 기능들
```kotlin
    @JvmStatic
    @Container
    val genericContainer = GenericContainer<Nothing>("dockerImageName")
        .apply {
            withEnv("POSTGRES_DB", "DATABASE_NAME")
            withExposedPorts(5432) // 포트를 선언하지만 실제값은 랜덤이다. 사용할 수있는 포트 중에서 랜덤으로
        }
    

    @Test
    internal fun `container port`() {
        val realPort = genericContainer.getMappedPort(5432) // 실제 컨테이너가 사용하는 포트
    }
```
* 도커 컨테이너 이름으로 테스트 도커 컨테이너명으로 생성할 수 있음
* 로컬 저장소에 있는 경우 로컬에 있는 컨테이너를 생성, 없는 경우 원격저장소에있는 도커이미지 이름으로 생성한다.


```kotlin
@BeforeAll
@JvmStatic
fun beforeAll() {
    val log by logger()
    val logConsumer = Slf4jLogConsumer(log)
    postgrContainer.followOutput(logConsumer)
}
```
* 컨테이너안에 있는 log를 보기 위해서 `Slf4jLogConsumer`를 이용할 수 있다.

```
00:34:41.759 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: fixing permissions on existing directory /var/lib/postgresql/data ... ok
00:34:41.759 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: 
00:34:41.759 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: Data page checksums are disabled.
00:34:41.760 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: creating subdirectories ... ok
00:34:41.760 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: 
00:34:41.760 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: The default text search configuration will be set to "english".
00:34:41.760 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: The default database encoding has accordingly been set to "UTF8".
00:34:41.761 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: The database cluster will be initialized with locale "en_US.utf8".
00:34:41.761 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: 
00:34:41.761 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: This user must also own the server process.
00:34:41.761 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: The files belonging to this database system will be owned by user "postgres".
00:34:41.786 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: selecting default max_connections ... 100
00:34:41.810 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: selecting default shared_buffers ... 128MB
00:34:41.810 [Test worker] DEBUG org.testcontainers.containers.output.WaitingConsumer - STDOUT: selecting dynamic shared memory implementation ... posix
```
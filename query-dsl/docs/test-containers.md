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
```yml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    wait-time-in-ms-when-sync-empty: 5
```

* `register-with-eureka: false` 유레카 서버에 자신을 등록하지 않는다.
* `fetch-registry: false` 레지스트리 정보를 로컬에 캐싱하지 않는다.
*  `wait-time-in-ms-when-sync-empty: 5` 서버가 요청을 받기 전 대기할 초기 시간
* 유레카는 등록된 서비스에서 10초 간격으로 연속 3회의 상태 정보를 받아야 하므로 등록 된 개별 서비스를 보여주는 데 30초가 걸린다. 서비스를 배포하고 테스트할 때 이 점을 유의해야 한다.

```kotlin
@SpringBootApplication
@EnableEurekaServer
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
```
* `@EnableEurekaServer` 스프링 서비스에 유레카 서버 활성화
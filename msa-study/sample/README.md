

```kotlin
@SpringBootApplication
@EnableCircuitBreaker // 서비스가 히스트릭스와 리본 라이버리를 사용한다
@EnableEurekaClient // 유레카의 서비스 디스커버리 에이전트에 서비스 자신을 등록하고 서비스 디스커버리를 사용해 원격 서비스의 위츠를 검색 하도록 지정한다
class SampleApplication

fun main(args: Array<String>) {
    runApplication<SampleApplication>(*args)
}
```
* `@EnableCircuitBreaker` 스프링 마이크로서비스에 이 애플리케이션에서 넷플릭스 히스트릭스 라이버르리가 사용된다고 알려 준다.
* `@EnableEurekaClient` 마이크로서비스 자신을 유레카 서비스 디스커버리에 에이전트에 등록하고 사비스 디스커버리를 사용해 코드에서 원격 REST 서비스의 엔드포인트를 검색할 것을 지정한다.

```kotlin
@RestController
@RequestMapping("hello")
class SampleController(val restTemplate: RestTemplate) {

    @HystrixCommand(threadPoolKey = "helloThreadPool")
    fun helloRemoteServiceCall(firstName: String, lastName: String): String {
        val restExchange = restTemplate.exchange(
                "http://logical-service-id/name/ca[{firstName}/{lastName}]",
                HttpMethod.GET, null, String::class.java, firstName, lastName
        )
        return restExchange.body!!
    }

    @GetMapping("/{firstName}/{lastName}")
    fun hello(@PathVariable firstName: String, @PathVariable lastName: String): String {
        return helloRemoteServiceCall(firstName, lastName)
    }
}
```
* `@HystrixCommand(threadPoolKey = "helloThreadPool")`는 두 가지 작업을 수행한다.
  * `helloRemoteServiceCall` 메서드가 호출될 때 직접 호출되지 않고 히스트릭스가 관리하는 스레드 풀에 위임한다. 호출이 너무 오래 걸리면(기본값 1초) 히스트릭스가 개입하고 호출을 중단시킨다. 이것이 회로 차단기 패턴의 구현이다.
  * 히스티릭스가 관리하는 `helloThreadPool` 이라는 스레드 풀을 만든다. `helloRemoteServiceCall` 메서드에 대한 모든 호출은 이 세르드 풀에서만 발새앟며, 수행 중인 다른 원격 서비스 호출과 격리 된다.
# 히스트릭스의 클라이언트 회복성 패턴
* 서비스 저하는 간혈적으로 발생하고 확신될 수 있다
  * 서비스 저하는 사소한 부분에 갑자기 발생할 수 있다 순식간에 애플리케이션 컨ㄴ테이너가 스레드 풀을 모두 소진해 완전히 무너지기 전까지 장애 징후는 일부 사용자가 문제점을 불평하는 정도로 나타날 것이다.
* 원격 서비스 호출은 대개 동기식이며 오래 걸리는 호출을 중단하지 않는다.
  * 서비스 호출자에게는 호출이 영구 수행되는 것을 방지하는 타임아웃 개념이 없다. 애플리케이션 개발자는 서비스를 호출해 작업을 수행하는 서비스가 응답할 때까지 대기한다.
* 애플리케이션은 대개 부분적인 저하가 아닌 원격 자원의 완전한 장애를 처리하도록 설계된다.
  * 서비스가 완전히 다운되지 않는다면 애플리케이션이 서비스를 계속 호출하고 빨리 실패하지 않는 일이 자주 발생한다. 애플리케이션은 제데로 동작하지 않은 서비스를 계속 호출할 것이다. 호출하는 애플리케이션이나 서비스는 정상적으로 저하될 수 있지만 자원 고갈로 비정상적으로 종료 될 가능성이 높다. **자원 고갈**이란 스레드 풀이나 데이터베이스 컨넥션 같은 재한된 자원이 한계를 넘으면 호출 클라이언트가 그 자원이 가용해질 때까지 대기 해야 하는 상황이다.

## 클라이언트 화복성 패턴이란?
클라이언트 회복성을 위한 소프트웨어 패턴은 원격 서비스가 에러를 던지거나 제대로 동작하지 못해 원격 자원의 접근이 실패할 때, 원격 자원(예를 들어 다른 마이크로서비스 호출 또는 데이터 베이스 검색)을 호출하는 클라이언트 충돌을 막는 데 초점이 맞추어져 있다. **이 패턴의 목적은 데이터베이스 컨넥션 및 스레드 풀 같은 소중한 클라이언의 소비자에게 상향전파되는 것을 막는다.**

### 클라이언트 회복성 패턴 네 가지
1. 클러이언트 측 부하 분산
   * 서비스 클라이언트는 서비스 디스커버리에서 조회한 마이크로서비스의 엔드포인트를 캐싱한다.
2. 회로 차단기 (circuit breaker)
   * 회로 차단기 패턴은 서비스 클라이언트가 장애 중인 서비스를 반복적으로 호출하지 못하게 한다.
3. 폴백 (fallback)
   * 호출이 실패하면 폴백 실행 가능한 대안이 있는지 확인한다.
4. 벌크헤드 (bulkhead)
   * 벌크헤드는 불량 서비스가 클라이언트의 모든 자원을 고갈시키니 않도록 서비스 클라이언트가 수행하는 서비스 호출을 격리한다.

### 클라이언트 측 부하 분산
클라이언트 측부하 분산은 클라이언트가 넷플릭스 유레카 같은 서비스 디스커버리 에이전트 이용해 서비스의 모든 인스턴스를 검색한 후 해당 서비스 인스턴스의 실제 위치를 캐싱한다.

클라이언트 측 로드 밸런서는 서비스 클라이언트와 서비스 소비자 사이에 위치하므로 서비스 인스턴스가 에럴,ㄹ 전달하거나 불량 동작하는지 감지한다. 클라이언트 측 로드 밸런서가 문제를 감지할 수 있다면 가용 서비스가 위치 풀에서 문제가 된 서비스 인스턴스를 제거해 서비스 호출이 그 인스턴스로 전달되는 것을 막는다.

### 회로 차단기
소프트웨어 회로 차단기는 원격 서비스 호출을 모니터링한다. 호출이 오래 걸리면 회로 차단기 중재해 호출은 중단한다. 회로 차단기 원격 자원에 대한 모든 호출을 모니터링하고, 호출이 필요한 만큼 실패하면 회로 차단기가 활성화되어 빨리 실패하게 만들며, 고장 난 원격 자원은 더 이상 호출되지 않도록 차단한다. 회로 차단 패턴이 제공하는 핵심 기능은 다음과 같다

* 빠른 실패 : 원격 서비스가 저하를 겪으면 애플리케이션은 빨리 실패함으로써 애플리케이션 전체를 다운 시킬 수 있는 자원 고갈 이슈를 방지한다. 대부분의 장애 상황에서 완전히 다운 되는 것보다 부분적으로 다운되는 것이 더 낫다.
* 원만한 실패: 타임아웃과 빠른 실패 방법을 사용하는 회로 차단기 패턴으로 애플리케이션 갤발자는 원만한하게 실패하거나 사용 의도로 수행하는 대체 메커니즘을 찾을 수 있다.
* 원활한 회복: 회로 차단기 패턴이 중재자 역할을 한다면 회로 차단기는 요청 자원이 온라인 상태인지 주기적으로 확인하고, 사람의 개입 없이 자원 접근을 다시 허용할 수있다.

### 폴백 처리
폴백 패턴을 사용하면 원격 서비스에 대한 호출이 실패할 때 예외를 발생시키지 않고 서비스 소비자가 대체 코드 경로를 실행해 다른 방법으로 작업을 수행할 수 있다. 일반적으로 이 패턴은 다른 데이터 소스에 데이터를 찾거나 향후 처리를 위해 사용자 요청을 큐에 입력하는 작업과 연관된다. 

### 벌크헤드
벌크 헤드 패턴을 적용하면 원격 자원에 대한 호출을 자원별 스레드 풀로 분리하므로 특정 원격 자원의 호출이 느려져서 전체 애플리케이션이 다운될 수 있는 위험을 줄일 수 있다. 스레드 풀은 서비스를 위한 벌크헤드 역할을 한다. 각 원격 자원은 분리되어 스레드 풀에 할당된다. 한 서비스가 가느리게 반응한다면 해당 서비스 호출을 위한 스레드 풀은 포화되어 요청을 처리하지 못하겠지만 다른 스레드에 풀을 할당된 다른 서비스 호출은 포화되지 않는다.

## 히스트릭스 회로 차단기 구현

```kotlin
@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
@EnableDiscoveryClient
class HystrixApplication

fun main(args: Array<String>) {
    runApplication<HystrixApplication>(*args)
}
```

```kotlin
@HystrixCommand
private fun getOrganization(organizationId: String): Organization {
    return getOrganizationRestClient.getOrganization(organizationId)
}
```
* `@HystrixCommand` 애너테이션을 사용하면 `getOrganization()` 메서드가 호출될 때마다 히스트릭스 회로 차단기와 해당 호출이 연결된다.
* 회로 차단기는 `getOrganization()` 메서드 호출이 1,000 걸릴 때마다 호출이 중단한다.

### 회로 차단기의 타임아웃 사용자 정의

```kotlin
@HystrixCommand(
            commandProperties = [HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10")]
    )
    fun getOrganization(organizationId: String): Organization {
        return getOrganizationRestClient.getOrganization(organizationId)
    }
```
* `commandProperties` 히스트릭스를 사용자 정의하기 우ㅢ해 추가 매게변수를 전달한다.
* `HystrixProperty` 회로 최단기의 타임아웃 시간을 설정하는 데 사용되는 프로퍼티
* `execution.isolation.thread.timeoutInMilliseconds` 프로퍼티를 사용해 히스트릭스 호출이 실패하기 전까지 대기할 최대 타임아웃 시간을 12초로 설정한다.

### 폴백 프로세싱
회로 차단기 패턴의 장점은 원격 자원의 소비자와 리소스 사이에 중간자를 두어 개발자에게 서비스 실패를 가로채고 다른 대안을 선택할 기회를 준다는 것이다.

```kotlin
    @HystrixCommand(fallbackMethod = "buildFallbackLicenseList")
    fun getLicenses(organizationId: String): List<String> {
        return getLicenses();
    }

    private fun buildFallbackLicenseList(organizationId: String) {
        return listOf<>(License()
                .withId("11232323")
                .withProductName("sample"))
    }
```
* `commandProperties` 속성을 추가하면 호출이 너무 오래 걸려 히스트릭스를 차단할 때 호출할 메서드 이름을 속성에 추가하면 된다.
* 폴백 메서드는 `@HystrixCommand` 보호하려는 메서드와 같은 클레스에 있어야한다.
* 모든 매개변수를 폴백이 받으므로 폴백 메서드는 이전 메서드와 서식이 완전히 동일 해야한다.
* `buildFallbackLicenseList`는 가짜 정보를 담고 있는 License 객체를 하나를 만드는들어 보낸다.

### 벌크헤드 패턴 구현
히스트릭스는 서로 다른 원격 자우너 호출 간에 벌크헤드를 생성하기에 용이한 메커니즘을 제공한다. 각 원격 지원 호출이 자기 스레드 풀을 이용해 스레드 풀마다 요청을 처리하는 데 필요한 최대 스레드 개수를 설정한다. 성능이 나쁜 서비스가 동일 스레드 풀 안에 있는 서비스 호출에만 영향을 미치미르 호출에서 발생할 수 있는 피해가 제한된다.

```kotlin
    @HystrixCommand(
            fallbackMethod = "buildFallbackLicenseList",
            threadPoolKey = "licenseByOrgThreadPool",
            threadPoolProperties = [
                HystrixProperty(name = "coreSize", value = "30"),
                HystrixProperty(name = "maxQueueSize", value = "30"),
            ]
    )
    fun getLicenses(organizationId: String): List<String> {
        return getLicenses();
    }
```
* threadPoolKey 속성은 스레드 풀의 고유 이름을 정의한다.
* coreSize 속성은 스레드 풀의 스레드 개수를 정의한다.
* maxQueueSize는 스레드 풀 앞에 배치할 큐와 큐에 넣을 요청 수를 정의한다.
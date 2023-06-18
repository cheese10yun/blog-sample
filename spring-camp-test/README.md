# 실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 1: 효율적인 Mock Test

[2023 Spring Camp 실무에서 적용하는 테스트 코드 작성 방법과 노하우](https://springcamp.ksug.org/2023/)에서 발표한 내용과 발표 시간 관계로 다루지 못했던 부분을 블로그로 포스팅했습니다. 효율적인 Mock Test 주제는 테스트 코드를 작성하다 보면 Mock Test를 작성해야 하는 경우가 발생하는데요. Mock 테스트 코드 작성에 어려움을 겪었던 경험과 그것을 해결하기 위해 시도한 방법들에 대해 전달하려 합니다. 해당 포스팅에서는 사전 지식으로 통합 테스트, 단위 테스트, Mock 테스트, 테스트 더블 등의 기본적인 개념에 대해서 다루지 않으며, 특정 라이브러리에 대한 사용법을 직접적으로 다루지 않습니다. 스프링 프레임워크 기반으로 2,3년 정도 테스트 코드를 작성했다면 이해하는데 큰 어려움은 없습니다.

## 기존 가맹점 등록 Flow

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/007.jpeg)

가맹점 관리 시스템에서 운영자가 가맹점을 등록을 진행한다고 가정해 보겠습니다. 시스템 초기에는 사업자명과, 가맹점명을 직접 입력해서 가맹점 서비스 서버에 저장하는 단순한 플로우로 구성되어 있습니다.

```kotlin
@Service
class ShopRegistrationService(
    private val shopRepository: ShopRepository
) {

    fun register(
        brn: String
    ): Shop {
        return shopRepository.save(
            Shop(
                brn = brn,
                name = response.body!!.name
            )
        )
    }
}
```

해당 플로우를 코드로 구현하면 입력받은 값을 그대로 영속화하는 코드 작성됩니다.

```kotlin
@Test
fun `Shop 등록 테스트 케이스`() {
    //given
    val brn = "000-00-0000"
    val name = "주식회사 XXX"

    //when
    val shop = shopRegistrationService.register(brn)

    //then
    then(shop.name).isEqualTo(name)
    then(shop.brn).isEqualTo(brn)
}
```

테스트 코드도 입력받은 값이 정상적으로 등록됐는지 확인하는 단순한 테스트 코드로 작성됩니다. 여기까지는 어렵지 않게 테스트 코드를 작성할 수 있습니다.

## 신규 가맹점 등록 Flow

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/011.jpeg)

서비스 규모가 점차 커지면서, 파트너 서비스가 독립적인 애플리케이션으로 분리되어, 사업자 번호만 입력하면 파트너 서비스에 질의하여 등록하는 플로우로 변경되었습니다.

```kotlin
@Service
class ShopRegistrationService(
    private val shopRepository: ShopRepository,
    private val partnerClient: PartnerClient
) {

    fun register(
        brn: String,
    ): Shop {
        val partner = partnerClient.getPartnetBy(brn)
        return shopRepository.save(
            Shop(
                brn = brn,
                name = partner.name
            )
        )
    }
}
```

## 신규 가맹점 등록 Mock Server 기반 Test Code

PartnerClient 객체로 파트너 서비스 서버와 HTTP 통신을 하여 가맹점명을 질의하여 영속화하는 방식으로 변경됩니다.

```kotlin

@Test
fun `가맹점 등록 Mock HTTP Test`() {
    //given
    val brn = "000-00-0000"
    val name = "주식회사 XXX"
    // (1) HTTP 통신 Mocking CODE
    mockServer
        .expect(
            requestTo("http://localhost:8080/api/v1/partner/${brn}")
        )
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    """
                        {
                          "brn": "${brn}",
                          "name": "${name}"
                        }
                    """.trimIndent()
                )
        )

    //when
    val shop = shopRegistrationService.register(brn)

    //then
    then(shop.name).isEqualTo(name)
    then(shop.brn).isEqualTo(brn)
}
```

구현 코드가 변경되었으니 테스트 코드도 변경이 필요합니다. 가맹점명을 PartnerClient에 의존했기 때문에 테스트 코드를 작성하기 위해서는 HTTP 통신을 Mocking 하여 테스트 코드를 작성합니다. (1) 코드를 보면 요청할 HTTP url, method, content-type, Request Body를 mocking 합니다. 즉 테스트 환경에서 HTTP 요청/응답을 Mocking 하여 테스트 코드를 변경합니다.

### Mock Server 기반 Test Code의 문제점

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/017.jpeg)

A Controller, A Service 객체에 직/간접적으로 PartnerClient 객체를 의존하고 있다고 가정해 봅시다. 그렇게 되면 기존 테스트 코드에 큰 변경이 발생합니다. PartnerClient를 직/간접적으로 의존하는 모든 테스트 코드에 Mock Server의 Mocking 작업이 필요해집니다. A Controller, A Service 두 개의 테스트 코드에서는 크게 문제가 없습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/019.jpeg)

하지만 그보다 의존관계가 많고 복잡하다면 어떻게 될까요? 또 현재는 의존 관계가 적지만 앞으로 늘어날 가능성이 높다면 어떻게 될까요? 요구사항이 변경되어 PartnerClient가 변경되면 어떻게 될까요? PartnerClient를 직/간 접적으로 의존하는 모든 구간에 가서 HTTP Mocking 관련 코드를 추가 및 변경해야 합니다. 위 예제 샘플 코드는 간단하지만 실제 요청 Request Body, Response Body가 몇십 줄에서 몇백 줄까지 넘어가는 코드들도 빈번하게 작성됩니다. 가장 큰 문제는 테스트 코드를 작성하는 복잡해지며 그로 인해 테스트 코드 작성을 기피하게 된다는 것입니다. 이런 문제를 해결하기 위해서 다른 방법을 찾아야 했습니다.

## 신규 가맹점 등록 @MockBean 기반 Test Code

Mock Server 기반 Test Code의 문제를 해결 방법으로는 생각한 것은 @MockBean을 사용하는 것입니다. @MockBean으로 Mock 객체를 주입받아 행위 자체를 Mocking 하는 것입니다.

```kotlin
@MockBean
private lateinit var partnerClient: PartnerClient

@Test
fun `register mock bean test`() {
    //given
    val brn = "000-00-0000"
    val name = "주식회사 XXX"
    // Mockito 기반으로 객체 행위를 Mocking, HTTP 통신 Mocking 보다 비교적 간단하게 구성이 가능하다.
    given(mockPartnerClientService.getPartner(brn))
        .willReturn(
            ResponseEntity(
                PartnerResponse(brn, name),
                HttpStatus.BAD_REQUEST
            )
        )

    //when
    val shop = shopRegistrationService.register(brn)

    //then
    then(shop.name).isEqualTo(name)
    then(shop.brn).isEqualTo(brn)
}
```

Mockito 기반으로 객체 행위를 Mocking, HTTP 통신 Mocking 보다 비교적 간단하게 구성이 가능합니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/023.jpeg)

물론 MockBean을 사용해도 관련 의존성이 있는 테스트 코드에 Mocking을 해야 한다는 사실은 변하지 않지만, 비교적 간단하게 Mocking을 구성할 수 있어 효율적인 방법이 될 수 있습니다.

### @MockBean 기반 Test Code 단점

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/026.jpeg)

해당 클래스의 테스트 코드만 돌려보면 문제는 없습니다. 하지만 전체를 대상으로 테스트 코드를 실행하면 문제가 발생할 수도 있습니다. 전체 테스트 코드 빌드 시에 각기 다른 객체를 MockBean을 사용하게 되면 Application Context가 재사용되지 못하고 MockBean을 변경하기 위해서 다시 초기화 작업을 진행하게 됩니다. 그 결과 Application Context가 N 번 초기화가 발생하게 됩니다. @MockBean 관련 테스트 코드를 작성하면 할수록 Application Context 초기화 이슈가 발생하는 구조이기 때문에 전체 테스트 빌드 시간이 느려지게 됩니다. 해당 문제를 해결하기 위해서 또 다른 방법을 찾아야 했습니다.

## 신규 가맹점 등록 @TestConfiguration 기반 Mock Bean Test Code

@MockBean으로 주입받을 객체를 교체하기 때문에 Application Context가 재사용하지 못한다면 그냥 해당 객체를 Mock 객체 자체를 실제 Bean으로 등록시키면 해결될 거 같았습니다.

```kotlin

@TestConfiguration
class ClientTestConfiguration {

    @Bean
    @Primary
    fun mockPartnerClient() = mock(PartnerClient::class.java)!!
}
```

Mockito의 mock 함수를 통해 PartnerClient 객체를 Bean으로 동록 시킵니다. 이때 실제 Bean과 겹칠 수도 있으니 Primary을 통해서 우선순위를 높이고, 해당 설정은 Test에서만 사용하기 때문에 test 디렉토리에 위치시키며 @TestConfiguration으로 설정합니다.

```kotlin
class ShopRegistrationServiceMockBeanTest(
    private val shopRegistrationService: ShopRegistrationService,
    // @MockBean에서 일반 Bean으로 변경
    private val partnerClient: PartnerClient
) : TestSupport() {

    @Test
    fun `register mock bean test`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        given(mockPartnerClient.getPartnerBy(brn))
            .willReturn(PartnerResponse(brn, name))

        //when
        val shop = shopRegistrationService.register(brn)

        //then
        then(shop.name).isEqualTo(name)
        then(shop.brn).isEqualTo(brn)
    }
}
```

변경된 코드를 살펴보겠습니다. 기존 @MockBean을 통해 주입했지만 이제 실제 Bean이기 때문에 생성자 주입으로 대체합니다. 여기서 주입받은 mockPartnerClient 객체는 TestConfiguration을 통해 등록된 Bean입니다. 이렇게 실제 Bean으로 등록 시킴으로써 Application Context 초기화 이슈를 해결할 수 있습니다.

### @TestConfiguration 기반 Mock Bean Test Code 멀티 모듈에서 적용

실제 대부분의 프로젝트가 멀티 모듈로 구성되어 있기 때문에 멀티 모듈에도 적용시켜보겠습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/037.jpeg)

다음과 같이 멀티 모듈로 구성돼 있다고 가정해 보겠습니다. HTTP Client 모듈에 PartnerClient 및 HTTP 통신 관련 코드들이 해당 모듈에 구성되어 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/039.jpeg)

위에서 작성한 ClientTestConfiguration 관련 코드는 HTTP Client 모듈의 test 디렉토리에 위치합니다. 해당 설정으로 PartnerClient 모듈의 Mock 객체를 Bean으로 사용이 가능합니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/042.jpeg)

Service A 모듈에서 HTTP Client 모듈을 의존하여 가맹점 등록 로직을 작성해야 하기 때문에 의존성을 추가했습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/044.jpeg)

이제 Service A 모듈에서 가맹점 등록 관련 테스트 코드를 작성하기 위해서는 HTTP Client 모듈의 test 디렉터리에 있는 ClientTestConfiguration 객체가 필요합니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/045.jpeg)

하지만 Service A 모듈의 test 디렉터리에서는 HTTP Client 모듈의 테스트 디렉터리에 접근할 수 없기 때문에 HTTP Client의 관련 의존성 테스트 코드를 작성할 수 없습니다. 물론 Service A 모듈에서 ClientTestConfiguration를 직접 정의하면 사용이 가능하지만 이렇게 구성하면 Service B, API App, Batch App 모듈에서도 계속 동일한 중복 코드가 발생하게 됩니다. 또 다른 문제를 만나게 되었고 이 문제를 해결하기 위한 방법을 찾아야 했습니다.

## 신규 가맹점 등록 fixtures 기반 Mock Bean Test Code

[java-test-fixtures](https://docs.gradle.org/current/userguide/java_testing.html#ex-applying-the-java-test-fixtures-plugin) 플러그인을 통해서 멀티 모듈 환경에서의 문제를 해결했습니다. 해달 플러그인에 대한 사용법에 대해서는 다루지 않겠습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/048.jpeg)

java-test-fixtures 플러그인 사용하면 testFixtures 디렉터리가 생성되며 기존 ClientTestConfiguration 코드를 testFixtures 디렉터리에 위치 시킵니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/051.jpeg)

Service A 모듈에서는 testFixtures을 통해서 테스트에 사용할 http-client 모듈을 의존성을 주입받게 되면 HTTP Client 모듈의 testFixtures 디렉터리에 있는 객체를 Service A 모듈의 test 디렉터리에서 사용할 수 있으며, main 디렉터리에서는 사용 수 없습니다. 즉 main 디렉터리에서는 import가 불가능하며, test 디렉터리에서는 import가 가능합니다.

## 꼭 이렇게 어렵게 구성을 해야 할까요?

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/056.jpeg)

HTTP Client 모듈의 main 디렉터리에 ClientConfiguration을 위치 시킴으로써, 해당 이슈는 쉽게 해결이 가능합니다. main 디렉터리에 있으니 당연히 Service A 모듈에서 HTTP Client의 test 디렉터리에 접근이 가능합니다. 하지만 문제가 있습니다. main 디렉터리에서도 접근이 가능하다는 것입니다. 테스트를 위해 만든 mock 객체인데도 접근이 가능하다는 것은 문제이며 테스트 코드를 작성하면서 다음과 같은 의문을 가져야 합니다.

> “테스트를 쉽게 하기 위해, 운영 코드 설계를 변경하는 것이 옳은가?”

저는 테스트 코드 작성을 중요하게 생각하지만 그것을 편리하게 작성하기 위해서 운영 코드의 설계를 변경 것은 바람직하지 않다고 생각합니다. 만약 저 Bean이 실수로라도 실제 운영 환경에 올라가게 되는 경우 큰 문제가 발생하며, 테스트를 쉽게 하기 위한 목적으로 실제 구현 코드 영역에 이런 코드들이 계속해서 추가된다면, 전체 애플리케이션에 악영향을 미칠 수 있습니다. 그렇기 때문에 먼 길을 돌아왔지만 그럴만한 가치 있다고 생각합니다.

## Mock Test 방법 정리

| 방식                 | 장점                                   | 단점                                       |
|--------------------|--------------------------------------|------------------------------------------|
| Mock Server Test   | HTTP 통신을 실제 진행하여 서비스 환경과 가장 근접한 테스트 | HTTP 통신 Mocking을 의존하는 모든 구간에 Mocking 필요  |
| @MockBean          | HTTP Mocking에 비해 비교적 간단하게 Mocking 가능 | Application Context를 재사용 못해 테스트 빌드 속도 저하 |
| @TestConfiguration | Application Context 이슈 해결            | 멀티 모듈 환경에서 @TestConfiguration 사용 어려움     |
| java-test-fixtures | 멀티 모듈에서 환경에서 사용 가능                   | 멀티 모듈이 아닌 경우 불필요                         |

## 테스트할 수 없는 영역 대처 자세

Mock Test를 중심으로 얘기했지만, 제가 전달하고자 하는 메시지 중 하나는 테스트할 수 없는 영역 대처 자세에 대한 것입니다. HTTP 통신처럼 꼭 제어할 수 없는 환경에 국한된 것은 아닙니다.

### 테스트 코드 작성이 불가능한 이유는 매우 다양하다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/060.jpeg)

Redis를 도입하게 되어 각각의 환경을 구성했지만 테스트 환경을 구축하지 못했다고 가정해 보겠습니다. 테스트 환경의 인프라스트럭처 구성이 되어 있지 못해서 테스트 코드 작성이 불가능합니다. 이처럼 테스트 코드를 작성하기 불가능하거나 어려운 이유는 매우 다양합니다. 해당 예시처럼 Redis 테스트 환경 구축에 대한 지식이 아직 없어 못하는 경우도 있을 수 있으며, 그 밖에 다양한 이유로 테스트 코드 작성이 불가능한 이유는 필연적으로 발생하며 그것에 대처하는 방향성에 대해서 말씀드리고 싶습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/065.jpeg)

xxx 이유로 테스트 코드 작성이 어려운 영역을 Black Box 영역이라 지칭하겠습니다. 이 Black Box 영역의 가장 큰 문제점은 이 영역이 전이된다는 것입니다. Redis 테스트 환경이 없다면 그것을 직/간접적으로 의존하는 구간이 Black Box로 전이됩니다. 이렇게 전이되다 보면 모든 영역이 테스트 불가능한 Black Box가 됩니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/images/069.jpeg)

Black Box 영역이 전이되지 않게 격리 시켜야 합니다. 설령 그 영역 자체는 테스트를 못하는 한이 있더라도 그 Black Box가 전이되는 것을 막아야 합니다. 즉, Black Box 영역을 테스트 못하더라도 다른 객체는 여전히 테스트를 진행할 수 있는 환경을 구성해야 합니다. 비단 Mock 관련에 한정된 것은 아닙니다. 이러한 설명을 가장 매끄럽게 할 수 있는 것이 Mock이라는 상황인 것이고, 전달하고자 하는 핵심 메시지는 테스트가 어렵 가나, 불가능한 영역이 전이되는 것을 격리 시키는 것입니다. 이렇게 격리 시킴으로써 다른 영역은 테스트가 가능 해진다는 것입니다. 격리 시키는 방법은 다양하게 있으며, 해당 프로젝트에 알맞은 적절한 방법을 적용해서 사용하면 됩니다.

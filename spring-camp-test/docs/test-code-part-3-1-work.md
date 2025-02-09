# 시작하며

안녕하세요. 정산플랫폼 윤입니다. [지난 시리즈 실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 1](https://tech.kakaopay.com/post/mock-test-code/)에서는 효율적인 Mock 테스트 진행 방법을, [Part 2: 테스트 코드로부터 피드백 받기](https://tech.kakaopay.com/post/mock-test-code-part-2/)에서는 테스트 코드가 주는 피드백을 통해 구현 코드를 개선하는 방법을, 그리고 [Part 3: Given 지옥에서 벗어나기 - 객체 기반 데이터 셋업의 한계](https://tech.kakaopay.com/post/given-test-code/)에서는 객체 기반 테스트 데이터 셋업이 가져오는 한계와 복잡성을 극복하는 전략에 대해 다루었습니다.

이번 "Given 지옥에서 벗어나기 - 스노우볼을 굴려라"에서는 앞서 제기된 문제점을 한 걸음 더 발전시켜, 작은 단위의 Given 절들이 모여 점차 큰 기능의 테스트를 완성해 나가는 스노우볼 효과를 실현하는 방법에 대해 추가적으로 살펴보겠습니다. 우리는 보통 작은 단위의 기능을 먼저 개발한 후, 이 기능들을 서로 연결해 보다 큰 기능을 수행하는 로직을 구현합니다. 이 과정에서 각 작은 기능에 대해 작성된 테스트 코드들은 해당 기능의 올바른 동작을 검증하는 동시에, 나중에 이 기능들을 활용해 더 큰 기능의 테스트를 작성할 때 중요한 기반이 됩니다.

즉, 이미 검증된 작은 기능들의 테스트 코드를 재사용하여 더 큰 기능을 검증하면, 테스트 코드 전체의 연결성이 자연스럽게 확보됩니다. 이러한 방식은 테스트 코드 주도 개발(TDD)을 지속적으로 진행하는 데에도 큰 도움이 됩니다. 과거에 정교하게 작성한 테스트 코드가 현재의 테스트 코드 작성을 위한 참고자료가 되고, 관련된 다른 영역의 테스트에도 긍정적인 영향을 미치면서, 마치 스노우볼이 점차 커져가는 것처럼 테스트 커버리지와 신뢰성을 확장할 수 있습니다.

반면, 모든 기능을 먼저 구현한 후 테스트 코드를 작성하려고 하면, 각 작은 기능의 세부적인 부분까지 꼼꼼하게 검증하기 어려워집니다. 그 결과, 테스트 코드가 단순한 의무감에 의한 의미 없는 코드로 전락할 위험이 있으며, 이로 인해 테스트의 진정한 장점을 충분히 누리지 못할 수 있습니다. 본 포스팅에서는 테스트 코드의 Given 절 작성시 테스트 코드의 재사용성과 연결성을 극대화하는 방법에 대해 살펴보겠습니다. 극대화하는 동시에, 전체 시스템의 신뢰성을 높이는 핵심 전략임을 다시 한번 강조하고 싶습니다.

## Given 지옥에 빠지다

테스트 코드를 작성할 때, **given 절 작성**에서 여러 가지 문제가 발생합니다.
우선, given 절에서는 다양한 데이터를 셋업해야 하며, 이 과정에 들어가는 코드량이 상당히 많습니다. 이러한 셋업 코드 자체가 테스트의 주요 관심사와 일치한다면 문제가 없겠지만, 실제로는 테스트하고자 하는 핵심 기능과 무관한 설정 코드들이 대부분을 차지하게 됩니다. 이로 인해 테스트 코드 작성이 번거로워지고, 결국 **테스트 코드의 가독성이 떨어지며** 폭넓은 테스트 케이스 작성을 저해하게 됩니다. 많은 경우, 의무감에 의한 최소한의 케이스만 작성되기 쉽습니다.

더욱이, 복잡한 given 절을 여러 모듈에서 재활용할 수 있는 방법을 마련하지 않으면, 동일한 셋업 코드를 계속 복사-붙여넣기 해야 하는 상황에 직면하게 됩니다. 그리고 외부 인프라에 의존하는 로직, 예를 들어 등록 결과를 외부로 전송하는 기능이 추가되면, 각 테스트 코드마다 **Mocking 기반**의 외부 호출 설정 코드가 추가되어야 합니다. 이 경우, 테스트 코드 내에 불필요한 Mocking 코드가 대량으로 포함되어, 테스트의 핵심 관심사와 무관한 코드들이 절대적인 코드량을 크게 증가시키게 됩니다.

이러한 문제들을 구체적으로 살펴보고, 해결 방안에 대해 논의해보겠습니다.

### 파라미터 지옥

예를 들어, 상품의 정보 변경에 따른 히스토리를 저장하는 **product_price_history** 테이블이 있다고 가정해봅시다. 이 테이블은 제품 내용이 변경되면 기존 히스토리의 종료일을 어제로 변경하고, 신규 히스토리를 생성하여 시작일을 오늘, 종료일을 미래로 설정하는 방식으로 운영됩니다.

아래는 이러한 상황을 테스트하기 위해 작성한 예제 코드입니다.

```kotlin
@Test
fun `기존 히스토리종료 신규히스토리생성 정상동작 테스트`() {
    // given
    val productHistory = ProductHistory(
        effectiveStartDate = LocalDate.of(2024, 1, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
        productId = 1L,
        productName = "Sample Product",
        category = "Electronics",
        brand = "Acme",
        price = 1_000.toBigDecimal(),
        discountRate = 0.toBigDecimal(),
        currency = "KRW",
        isActive = false,
        region = "North America",
        description = "Initial price record for Sample Product",
        cost = BigDecimal("250.00"),
        supplier = "Acme Supplier",
        taxRate = BigDecimal("0.08"),
        unit = "piece",
        additionalFee = 10.toBigDecimal(),
        // ... 관련 필드 생략 
    )

    persist(productHistory) // DB에 저장

    // when
    // 기존 히스토리를 종료시키고, 새로운 히스토리를 생성한다.
    productReservationService.renewReservationRecord(
        id = productHistory.id!!,
        effectiveStartDate = LocalDate.of(2024, 5, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
    )

    // then
    // DB에서 조회하여, 기존 히스토리 종료 검증
    // DB에서 조회하여, 새로운 히스토리 생성 검증
}
```

위 코드는 실제로는 더 많은 ProductHistory 관련 필드를 포함하지만, 테스트의 핵심 관심사는 **기존 히스토리를 종료하고 새로운 히스토리를 생성하는 기능**에 있습니다.  
문제는 테스트 코드 작성 시 모든 필드를 채워 넣어야 하므로 불필요한 파라미터들이 산재하게 되어 코드량이 많아지고, 그로 인해 테스트 코드 작성이 매우 번거롭고 복잡해진다는 점입니다. 절대적인 코드량이 많기 때문에 테스트 코드 작성이 불편해지며, 이러한 이유로 테스트 코드 작성을 기피하는 경우도 발생할 수 있습니다. 또한, ProductHistory에 새로운 파라미터가 추가될 경우 기존의 모든 테스트 코드를 수정해야 하므로, 변경 시 추가적인 작업이 많이 발생하는 단점이 있습니다.

이러한 문제를, 여기서는 편의상 '**파라미터 지옥**'이라고 칭하겠습니다.

### 모듈 분리로 인한 테스트 코드 재사용 지옥

파라미터 지옥을 피하기 위해 각 테스트마다 반복되는 테스트 데이터를 생성하는 헬퍼 함수를 사용하면 테스트 코드를 간결하게 작성할 수 있습니다. 예를 들어, 아래와 같이 `givenProductHistory` 함수를 정의하여 ProductHistory 인스턴스를 생성하면, 개별 테스트마다 복잡한 파라미터들을 일일이 입력할 필요가 없어집니다.

```kotlin
@Test
fun `기존 히스토리종료 신규히스토리생성 정상동작 테스트`() {
    // given
    val productHistory = givenProductHistory(
        effectiveStartDate = LocalDate.of(2024, 1, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
    )

    persist(productHistory) // DB에 저장

    // when
    // 기존 히스토리를 종료시키고, 새로운 히스토리를 생성한다.
    productReservationService.renewReservationRecord(
        id = productHistory.id!!,
        effectiveStartDate = LocalDate.of(2024, 5, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
    )

    // then
    // DB에서 조회하여, 기존 히스토리 종료 검증
    // DB에서 조회하여, 새로운 히스토리 생성 검증
}

fun givenProductHistory(
    effectiveStartDate: LocalDate,
    effectiveEndDate: LocalDate,
): ProductHistory {
    return ProductHistory(
        effectiveStartDate = effectiveStartDate,
        effectiveEndDate = effectiveEndDate,
        productId = 1L,
        productName = "Sample Product",
        category = "Electronics",
        // ... 기타 필드 생략
    )
}
```

그러나 이런 방식에도 한계가 있습니다. Gradle 멀티 모듈 프로젝트에서는 보통 `api(project(":모듈명"))`과 같이 설정하여 해당 모듈의 `src/main` 코드를 공유할 수 있습니다.  
즉, 각 모듈의 프로덕션 코드(비즈니스 로직 등)는 쉽게 재사용 및 공유할 수 있도록 공개됩니다.

하지만 테스트 코드는 별도의 `src/test` 소스셋에 위치하며, 이 소스셋은 기본적으로 외부 모듈에 노출되지 않습니다.  
즉, 한 모듈에서 정의한 테스트 전용 헬퍼 함수(예: `givenProductHistory`)는 다른 모듈에서 직접 참조하거나 재사용할 수 없습니다.  
이러한 제약으로 인해, API 모듈, 서비스 모듈, 외부 통신 모듈, 배치 모듈 등 각 모듈에서 동일한 테스트 데이터를 생성하기 위한 Given 코드가 중복되어 작성되어야 합니다.

이와 같이, 테스트 코드가 담긴 `src/test`는 모듈 간 공유가 불가능하기 때문에, 동일한 테스트 헬퍼 코드를 각 모듈마다 복사해야 하는 상황이 발생합니다. 이로 인해 발생하는 코드 중복과 관리의 어려움을 우리는 **모듈 분리로 인한 테스트 코드 재사용 지옥**이라고 부르겠습니다.

### 외부 인프라 의존으로 인한 Mocking 지옥

상품에 대한 적용일이 시작되면 담당자에게 이메일을 전송하는 기능이 추가되었다고 가정해봅니다. 이 경우, HTTP POST 요청을 통해 외부 서버로 상품 변경 알림 메일 전송 요청을 보내게 되며, 이 요청의 Request Body는 ProductHistory의 필드와 12개 정도 겹치는 15개 이상의 필드를 포함합니다.

```kotlin
data class ProductChangeNotificationRequest(
    // ProductHistory와 겹치는 필드 12개
    val effectiveStartDate: LocalDate,  // 변경 이력의 시작일
    val effectiveEndDate: LocalDate,    // 변경 이력의 종료일
    val productId: Long,                // 제품 식별자
    val productName: String,            // 제품 이름
    val category: String,               // 제품 카테고리
    val brand: String,                  // 브랜드 정보
    val price: BigDecimal,              // 가격
    val discountRate: BigDecimal?,      // 할인율
    val currency: String,               // 통화
    val region: String,                 // 적용 지역
    val cost: BigDecimal?,              // 원가
    val supplier: String?,              // 공급업체 정보

    // 추가 필드 3개: 알림 메일 전송에 필요한 정보
    val mailSubject: String,            // 메일 제목
    val mailBody: String,               // 메일 본문
    val recipientEmail: String          // 수신자 이메일 주소
)
```

예를 들어, 아래의 테스트 코드는 기존 히스토리를 종료하고 신규 히스토리를 생성한 후, 외부 HTTP 서버에 상품 변경 알림 메일 전송 요청을 보내는 로직을 포함합니다.

```kotlin
@Test
fun `기존 히스토리종료 신규히스토리생성 정상동작 테스트`() {
    // given
    val productHistory = givenProductHistory(
        effectiveStartDate = LocalDate.of(2024, 1, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
    )
    persist(productHistory) // DB에 저장

    given(
        emailSender.sendProductChangeNotificationEmail(
            ProductChangeNotificationRequest(
                effectiveStartDate = LocalDate.of(2024, 5, 1),
                effectiveEndDate = LocalDate.of(2025, 1, 1),
                productId = productHistory.productId,
                productName = productHistory.productName,
                category = productHistory.category,
                brand = productHistory.brand,
                price = productHistory.price,
                discountRate = productHistory.discountRate,
                currency = productHistory.currency,
                region = productHistory.region,
                cost = productHistory.cost,
                supplier = productHistory.supplier,
                // ... 기타 필드 생략 (총 15개 이상의 필드 존재)
                mailSubject = "Price Change Notification ...",
                mailBody = "Price of the product has been changed ...",
                recipientEmail = ""
            )
        )
    ).willReturn(true)

    // when
    // 기존 히스토리를 종료시키고, 새로운 히스토리를 생성한다.
    productReservationService.renewReservationRecord(
        id = productHistory.id!!,
        effectiveStartDate = LocalDate.of(2024, 5, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
    )

    // then
    // DB에서 조회하여, 기존 히스토리 종료 검증
    // DB에서 조회하여, 새로운 히스토리 생성 검증
}
```

문제는 이처럼 외부 요청으로 연결되는 경우, HTTP Request Body가 ProductHistory의 필드와 대부분 겹치게 되어 추가적인 필드들이 많아집니다. 그 결과, 테스트 코드 작성 시 매번 `sendProductChangeNotificationEmail` 호출에 필요한 모든 필드를 정확하게 채워 넣어야 하므로, 테스트 코드가 지나치게 복잡해지고 번거로워집니다.

또한, 우리가 관심 있는 핵심 기능은 기존 히스토리의 종료와 신규 히스토리의 생성인데, 이렇게 많은 필드를 포함한 외부 요청 로직이 추가되면 테스트 코드 내에서 본래 테스트하려던 핵심 의도가 희석되어, 테스트 코드의 가독성과 유지 관리성이 크게 떨어지는 문제가 발생합니다.

이러한 상황에서는 행위 기반(Mock)으로 접근하여, 외부 HTTP 호출 부분을 최소한의 인자와 검증으로 대체하는 것이 효율적입니다. 이를 통해 테스트의 핵심 관심사에 집중할 수 있으며, 불필요한 파라미터 작성으로 인한 번거로움을 줄일 수 있습니다.

## Given 지옥에서 벗어나기

테스트 코드를 작성할 때, **Given 절**에서 다양한 데이터를 셋업해야 하는 문제가 발생합니다. 이때 셋업 코드의 양이 방대해지면, 테스트 코드 작성이 번거로워지고 핵심 관심사 외의 불필요한 코드가 많이 포함되어 가독성이 떨어지게 됩니다. 또한, 이런 셋업 코드를 모듈 간에 재사용할 수 없는 경우, 각 모듈마다 동일한 Given 코드가 중복 작성되어 관리의 어려움을 초래합니다.

이를 해결하기 위해서는 테스트 코드의 재사용성을 높여 **Given 절을 최소화**하고, 핵심 테스트 로직에 집중할 수 있는 방법이 필요합니다.

그런데 Gradle 멀티 모듈 프로젝트에서는 **src/main** 코드는 `api(project(":모듈명"))` 설정을 통해 쉽게 공유할 수 있지만, 테스트 코드가 위치한 **src/test**는 기본적으로 외부 모듈에 노출되지 않습니다.  
따라서, 한 모듈에서 정의한 테스트 전용 헬퍼 함수(예: **`givenProductHistory`**)는 다른 모듈에서 재사용할 수 없어, 동일한 셋업 코드를 중복 작성해야 하는 상황이 발생합니다. 이 문제를 우리는 **모듈 분리로 인한 테스트 코드 재사용 지옥**이라고 부릅니다.

이를 극복하기 위한 한 가지 방안은 **`java-test-fixtures`** 라이브러리를 활용하는 것입니다.

### java-test-fixtures 라이브러리

**java-test-fixtures** 라이브러리는 테스트 전용 코드와 데이터를 별도의 소스셋인 **src/testFixtures**에 작성하여, 이를 여러 모듈 간에 공유할 수 있도록 도와줍니다.  
즉, **src/testFixtures**에 위치한 테스트 헬퍼 클래스나 데이터를 통해, 기존 **src/test**에 있는 DomainFixture 객체와 달리 외부 모듈에서도 쉽게 참조하고 재사용할 수 있게 됩니다.

예를 들어, 아래와 같은 디렉터리 구조에서 **src/testFixtures**는 모든 모듈에서 공통으로 사용할 수 있는 테스트 데이터를 제공합니다.

```
└── src
    ├── main
    │   └── kotlin
    │       └── com/spring/camp/http/PartnerClient.kt
    ├── test
    │   └── kotlin
    │       └── com/spring/camp/http/DomainFixture.kt
    └── testFixtures
        ├── kotlin
        │   └── com/spring/camp/http/SharedDomainFixture.kt
        └── resources
            └── META-INF/spring.factories
```

**참고:** 이 포스팅에서는 **java-test-fixtures** 라이브러리에 대한 간단한 소개만 진행하며, 구체적인 사용 방법은 다루지 않습니다.

이 방식을 통해, Given 절 작성에서 발생하는 불필요한 중복 코드와 복잡성을 해소하여, 보다 간결하고 유지보수하기 쉬운 테스트 코드를 작성할 수 있습니다.

### 파라미터 지옥 벗어 나기

### 모듈간 지옥 벗어 나기

### Mocking 지옥

## 스노우 볼을 굴려라


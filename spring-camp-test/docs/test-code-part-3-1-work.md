## 시작하며

안녕하세요. 정산플랫폼 윤입니다. [지난 시리즈 실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 1](https://tech.kakaopay.com/post/mock-test-code/)에서는 효율적인 Mock 테스트 진행 방법을, [Part 2: 테스트 코드로부터 피드백 받기](https://tech.kakaopay.com/post/mock-test-code-part-2/)에서는 테스트 코드가 주는 피드백을 통해 구현 코드를 개선하는 방법을, 그리고 [Part 3: Given 지옥에서 벗어나기 - 객체 기반 데이터 셋업의 한계](https://tech.kakaopay.com/post/given-test-code/)에서는 객체 기반 테스트 데이터 셋업의 한계와 복잡성을 극복하는 전략에 대해 다루었습니다.

이번 "Part 3: Given 지옥에서 벗어나기 - 스노우볼을 굴려라"에서는 **테스트 코드 작성 시 번거롭고 복잡하게 구성되는 Given 절을 보다 쉽게 작성할 수 있도록, 작성한 Given 절을 재활용하고 타 모듈에서도 공유할 수 있는 전략에 대해 집중적으로 살펴보겠습니다. 이를 통해 불필요한 반복 코드를 제거하고, 핵심 테스트 로직에만 집중하여 다양한 테스트 케이스를 손쉽게 확보할 수 있는 방법을 제시하고자 합니다.**

마지막으로, 스노우볼을 굴려라에서는 테스트 코드를 꾸준하게 작성할 수 있는 전략을 제시하고자 합니다. 작은 단위의 기능에 대한 테스트 코드를 작성함으로써, 그 기능들을 기반으로 더 큰 로직의 테스트를 구성할 때 직접적인 긍정적 영향을 미치는 것이 매우 중요합니다. 만약 모든 로직을 완성한 후에 통합 테스트 성격의 코드만 작성한다면, 테스트 코드 작성이 부담스러워지고 실질적인 이득을 느끼기 어려워집니다. 테스트 코드를 꾸준하게 작성하기 위해서는 이전에 작성했던 테스트 코드가 실제로 나에게 이점을 가져다 주어야 하며, 단순히 안전성을 보장한다는 막연한 이점이 아니라 체감할 수 있는 구체적인 이점이 있어야 지속적으로 스노우볼을 굴릴 수 있습니다. 그렇게 점진적으로 스노우볼을 굴리다 보면, 다양한 테스트 케이스를 확보하고 점차 테스트 커버리지와 신뢰성을 크게 확장시킬 수 있게 됩니다.

이러한 전략들을 바탕으로, 본 포스팅에서는 테스트 코드의 Given 절 작성의 재사용성과 모듈 간 공유를 극대화하여, 복잡하고 반복되는 셋업 코드를 단순화하는 방법을 상세히 다루겠습니다.

> 해당 포스팅은 스프링 프레임워크 기반에서 2,3년 정도 테스트 코드를 작성해 본 분들, 단위 테스트, Mock 테스트, 통합 테스트, 테스트 더블에 대한 기본적인 개념을 알고 있는 분들을 대상으로 합니다. 특정 라이브러리의 직접적인 사용법은 다루지 않습니다.

## Given 지옥에 빠지다

테스트 코드를 작성할 때, **Given 절 작성**에서 여러 가지 문제가 발생합니다.
우선, Given 절에서는 다양한 데이터를 셋업해야 하며, 이 과정에 들어가는 코드량이 상당히 많습니다. 이러한 셋업 코드 자체가 테스트의 주요 관심사와 일치한다면 문제가 없겠지만, 실제로는 테스트하고자 하는 핵심 기능과 무관한 설정 코드들이 대부분을 차지하게 됩니다. 이로 인해 테스트 코드 작성이 번거로워지고, 결국 **테스트 코드의 가독성이 떨어지며** 폭넓은 테스트 케이스 작성을 저해하게 됩니다. 많은 경우, 의무감에 의한 최소한의 케이스만 작성되기 쉽습니다.

더욱이, 복잡한 Given 절을 여러 모듈에서 재활용할 수 있는 방법을 마련하지 않으면, 동일한 셋업 코드를 계속 복사-붙여넣기 해야 하는 상황에 직면하게 됩니다. 그리고 외부 인프라에 의존하는 로직, 예를 들어 등록 결과를 외부로 전송하는 기능이 추가되면, 각 테스트 코드마다 **Mocking 기반**의 외부 호출 설정 코드가 추가되어야 합니다. 이 경우, 테스트 코드 내에 불필요한 Mocking 코드가 대량으로 포함되어, 테스트의 핵심 관심사와 무관한 코드들이 절대적인 코드량을 크게 증가시키게 됩니다.

이러한 문제들을 구체적으로 살펴보고, 해결 방안에 대해 논의해보겠습니다.

### 파라미터 지옥

예를 들어, 상품의 정보 변경에 따른 히스토리를 저장하는 `product_history` 테이블이 있다고 가정해봅시다. 이 테이블은 제품 내용이 변경되면 기존 히스토리의 종료일을 설정하고, 신규 히스토리를 생성하여 변경된 내용을 저장하는 방식으로 운영됩니다.

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

위 코드는 실제로 더 많은 `ProductHistory` 관련 필드를 포함하고 있지만, 테스트의 핵심 관심사는 **기존 히스토리를 종료하고 새로운 히스토리를 생성하는 기능**에 있습니다.

문제는 테스트 코드를 작성할 때 모든 필드를 채워 넣어야 하여, **주요 관심사와 무관한 파라미터들이 산재하게 됩니다.** 이로 인해 코드량이 과도해지고, 테스트 코드 작성이 매우 번거롭고 복잡해지며 **테스트의 핵심 의도를 파악하기 어려워집니다.**

게다가, `ProductHistory`에 새로운 파라미터가 추가되면 기존의 모든 테스트 코드를 수정해야 합니다. 테스트 코드 작성이 많을수록 추가 변경 작업이 크게 증가하여 오히려 악영향을 미치게 됩니다.

본 포스팅에서는 이러한 문제를 **파라미터 지옥**이라고 부르겠습니다.

### 멀티 모듈 환경에서의 테스트 코드 재사용 지옥

파라미터 지옥을 피하기 위해 각 테스트마다 반복되는 테스트 데이터를 생성하는 헬퍼 함수를 사용하면 테스트 코드를 간결하게 작성할 수 있습니다. 예를 들어, 아래와 같이 `givenProductHistory` 함수를 정의하여 `ProductHistory` 인스턴스를 생성하면, 개별 테스트마다 복잡한 파라미터들을 일일이 입력할 필요가 없어집니다.

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

그러나 이러한 방식은 멀티 모듈 구조에서 한계가 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/docs/image/image-1.png)

Gradle 멀티 모듈 프로젝트에서는 domain 모듈의 `src/test`에 작성된 테스트 전용 코드를 다른 모듈에서 참조할 수 없으므로, 동일한 테스트 데이터 생성 코드를 각 모듈마다 복사해야 하는 상황이 발생합니다. 이로 인해 테스트 코드의 재사용성이 떨어지고, 작성이 번거로워지며, 가독성과 유지보수성이 크게 저하됩니다.

즉, 한 모듈에서 정의한 테스트 전용 헬퍼 함수(예: `givenProductHistory`)는 API, 서비스, 외부 통신, 배치 모듈 등 다른 모듈에서 직접 참조하거나 재사용할 수 없습니다.

이러한 제약 때문에 동일한 테스트 데이터를 생성하기 위한 Given 코드가 각 모듈에 중복되어 작성되어야 하며, 이로 인한 코드 중복과 관리의 어려움을 본 포스팅에서는 **멀티 모듈 환경에서의 테스트 코드 재사용 지옥**이라고 부르겠습니다.

### 외부 인프라 의존으로 인한 Mocking 지옥

상품의 적용일이 시작되면 담당자에게 이메일을 전송하는 기능이 추가되었다고 가정해봅니다. 이 경우, HTTP POST 요청을 통해 외부 서버로 상품 변경 알림 메일 전송 요청을 보내게 되며, 이 요청의 Request Body는 `ProductHistory`의 필드와 12개 정도 겹치는 15개 이상의 필드를 포함합니다.

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

아래의 테스트 코드는 기존 히스토리를 종료하고 신규 히스토리를 생성한 후, 외부 HTTP 서버에 상품 변경 알림을 전송하는 부분을 mocking하여 작성한 예제입니다.

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
                recipientEmail = "sample@sample.test"
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

문제는 이처럼 테스트의 핵심 관심사가 아닌 Mocking 코드가 많이 포함되어, 테스트 코드의 가독성이 떨어지고 작성이 번거로워진다는 점입니다. 또한, 다양한 테스트 케이스를 작성해야 하는 복잡한 로직의 경우, 매번 `sendProductChangeNotificationEmail` 호출에 필요한 모든 필드를 정확하게 채워 넣어야 하므로 테스트 코드가 지나치게 복잡해집니다.

이와 같은 복잡성으로 인해, `sendProductChangeNotificationEmail`의 성공 및 실패 등 다양한 케이스를 충분히 다루기 어려워지며, 결국 폭넓은 테스트 케이스를 작성하는 데 큰 부담이 발생합니다.

이러한 문제를 본 포스팅에서는 **외부 인프라 의존으로 인한 Mocking 지옥**이라고 부르겠습니다.

## Given 지옥에서 벗어나기

본 포스팅에서 다루는 테스트 코드 작성 시 발생하는 문제점들은 다음 세 가지로 정리할 수 있습니다.

- **파라미터 지옥**  
  Given 절에서 모든 파라미터를 일일이 수동으로 지정해야 하는 번거로움이 존재합니다. 이로 인해 테스트 코드가 지나치게 복잡해지고, 테스트의 핵심 관심사(예: 기존 히스토리 종료 및 신규 히스토리 생성)가 묻혀 버리며, `ProductHistory`에 새로운 필드가 추가될 경우 모든 테스트 코드를 수정해야 하는 유지보수 부담이 증가합니다.
- **멀티 모듈 환경에서의 테스트 코드 재사용 지옥**  
  Gradle 멀티 모듈 프로젝트에서는 domain 모듈의 `src/test`에 작성된 테스트 전용 코드를 다른 모듈에서 직접 참조할 수 없으므로, 동일한 테스트 데이터 생성 코드를 각 모듈마다 복사해야 하는 상황이 발생합니다. 이로 인해 테스트 코드의 재사용성이 떨어지고, 중복 작성으로 인해 가독성과 관리의 어려움이 증가합니다.
- **외부 인프라 의존으로 인한 Mocking 지옥**  
  외부 서버에 이메일 전송 등 HTTP 요청을 위한 Mocking 작업 시, `ProductHistory`의 필드와 중복되는 부분이 많아 매번 모든 필드를 채워 넣어야 합니다. 이로 인해 테스트 코드가 불필요하게 복잡해지고, 실제 테스트하고자 하는 핵심 관심사가 희석되며, 복잡한 목킹 코드로 인해 테스트 작성 부담이 가중됩니다.

이러한 문제들을 극복하기 위해, `DomainFixture`와 `DomainIoFixture`를 통해 공통된 테스트 데이터를 재활용하고, `java-test-fixtures` 라이브러리를 활용하여 모듈 간 테스트 전용 코드를 공유하면, 불필요한 반복 작업과 복잡한 셋업 코드를 크게 줄일 수 있습니다. 이를 통해 핵심 테스트 로직에 집중하며, 전체 테스트 커버리지와 시스템의 신뢰성을 효과적으로 향상시킬 수 있습니다.

### java-test-fixtures를 통한 테스트 리소스 공유

`java-test-fixtures` 라이브러리는 테스트 코드 작성 시 반복적으로 필요한 객체 생성이나 데이터 초기화 작업을 손쉽게 처리할 수 있도록 도와주는 도구입니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/docs/image/image-2.png)

이 라이브러리를 활용해 `src/testFixtures` 디렉터리에 테스트 전용 코드를 작성하면, 다른 모듈에서도 해당 코드를 손쉽게 참조할 수 있습니다. 이를 통해 모듈 간 테스트 코드를 효과적으로 공유하고, 테스트 데이터 생성 코드를 재사용할 수 있게 됩니다. 이러한 전략을 활용하여 Given 지옥에서 벗어나는 방법에 대해 구체적으로 살펴보겠습니다.

> [이전 포스팅 - java-test-fixtures 기반 테스트 의존성 제공](https://tech.kakaopay.com/post/mock-test-code/#java-test-fixtures-%EA%B8%B0%EB%B0%98-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%9D%98%EC%A1%B4%EC%84%B1-%EC%A0%9C%EA%B3%B5) 포스팅을 참고하시면 더 자세한 내용을 확인하실 수 있습니다.

### 파라미터 지옥 벗어 나기

테스트 코드를 작성할 때, Given 절에 많은 파라미터를 모두 지정해야 하는 번거로움이 존재합니다. 이를 해결하기 위해 `DomainFixture` 클래스를 활용하여 `default` 값을 가진 헬퍼 메서드를 제공하면, 테스트 코드의 Given 절을 훨씬 간결하게 작성할 수 있습니다. 예를 들어, `productHistory` 메서드는 실제 값과 유사한 기본값들을 지정해두어, 테스트의 주요 관심사인 기존 히스토리 종료와 신규 히스토리 생성에 필요한 파라미터만 명시적으로 설정하고 나머지는 `default` 값으로 처리할 수 있도록 합니다. 이 방식을 사용하면 테스트 코드가 얼마나 간결해지는지 살펴보겠습니다.

```kotlin
object DomainFixture {

    fun productHistory(
        productId: Long = 1L,
        productName: String = "Sample Product",
        category: String = "Electronics",
        brand: String = "Acme",
        price: BigDecimal = 1_000.toBigDecimal(),
        discountRate: BigDecimal = 0.toBigDecimal(),
        effectiveStartDate: LocalDate = LocalDate.of(2024, 1, 1),
        effectiveEndDate: LocalDate = LocalDate.of(2025, 1, 1),
        currency: String = "KRW",
        isActive: Boolean = false,
        region: String = "North America",
        description: String? = "Initial price record for Sample Product",
        cost: BigDecimal? = BigDecimal("250.00"),
        supplier: String? = "Acme Supplier",
        taxRate: BigDecimal? = BigDecimal("0.08"),
        unit: String = "piece",
        additionalFee: BigDecimal = 10.toBigDecimal(),
    ): ProductHistory {
        return ProductHistory(
            productId = productId,
            productName = productName,
            category = category,
            brand = brand,
            price = price,
            discountRate = discountRate,
            effectiveStartDate = effectiveStartDate,
            effectiveEndDate = effectiveEndDate,
            currency = currency,
            isActive = isActive,
            region = region,
            description = description,
            cost = cost,
            supplier = supplier,
            taxRate = taxRate,
            unit = unit,
            additionalFee = additionalFee,
        )
    }
}
```

기존 테스트에서는 모든 필드를 일일이 채워 넣어야 했지만, `DomainFixture`를 활용하면 필요한 파라미터만 지정하고 나머지는 `default` 값으로 처리할 수 있어 테스트 코드가 훨씬 간결해집니다. 예를 들어, 기존 히스토리 종료와 신규 히스토리 생성이 주요 관심사인 테스트에서는 적용 시작일과 종료일만 명시적으로 지정하고 나머지는 `default` 값으로 처리할 수 있습니다.

```kotlin
@Test
fun `기존 히스토리종료 신규히스토리생성 정상동작 테스트`() {
    // given
    val productHistory = DomainFixture.productHistory(
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
```

테스트 코드는 매우 간결해졌으며, Given 절의 복잡성이 크게 줄어든 것을 확인할 수 있습니다. 이로 인해 테스트의 주요 관심사가 명확해져, 테스트 코드의 의도가 보다 쉽게 전달되고 기존 테스트 코드가 깨질 시에도 효과적으로 대응할 수 있습니다.

또한, `ProductHistory` 객체에 새로운 필드가 추가된다면, 오직 `DomainFixture`의 `productHistory` 메서드만 수정하면 되므로 기존 테스트 코드에는 변경사항이 없어집니다. 이를 통해 테스트 코드의 유지보수성이 크게 향상됩니다.

더불어, 객체 간 연관 관계가 복잡한 경우에도 `DomainFixture` 클래스를 활용하면 쉽게 설정할 수 있습니다. 예를 들어, `product`와 `productHistory` 간의 연관 테스트에서는 `product`를 먼저 영속화한 후 해당 `product`의 ID만 지정하고 나머지 필드는 기본값으로 처리하여 테스트 코드를 간결하게 유지할 수 있습니다. 아래와 같이 작성할 수 있습니다.

```kotlin
@Test
fun `product, productHistory 연관 테스트`() {
    // given
    val product = persist(DomainFixture.product()) // product DB에 저장
    val productHistory = DomainFixture.productHistory(
        productId = product.id!!, // product 연결
        effectiveStartDate = LocalDate.of(2024, 1, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
    )
    persist(productHistory) // DB에 저장

    // 기타 테스트 코드...
}
```

만약 `Product`와 `ProductHistory` 간의 연관 테스트가 필요하고, `product` 객체의 상세 정보보다는 단순 연결만 확인한다면, `default` 값을 활용하여 Given 절을 간결하게 작성할 수 있습니다.

이 방법을 사용하면, 연관 객체가 필요한 경우에도 `DomainFixture` 클래스를 통해 손쉽게 객체를 생성할 수 있으며, 복잡한 객체 셋업 없이 필요한 부분만 오버라이드하여 핵심 테스트 로직에 집중할 수 있습니다. 결과적으로, **이 방식은 복잡한 셋업으로 인해 테스트 코드가 흐려지는 문제를 해결하고, 테스트의 주요 관심사를 명확하게 유지할 수 있도록 도와줍니다.**

### 멀티 모듈 환경에서의 테스트 코드 재사용 지옥 벗어나기

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/docs/image/image-2.png)

`java-test-fixtures` 라이브러리를 사용하면 모듈 간 테스트 코드를 효과적으로 공유할 수 있습니다. 테스트 전용 코드를 작성할 모듈에 아래와 같이 플러그인을 추가하면:

```kotlin
plugins {
    id("java-test-fixtures")
}
```

이렇게 하면 `src/testFixtures` 디렉터리가 생성되며, 예를 들어 domain 모듈에 이 플러그인을 추가한 경우, `DomainFixture` 객체를 작성하여 Given 절을 보다 편리하게 구성할 수 있습니다. domain 모듈 내부에서는 `src/testFixtures`에 바로 접근할 수 있지만, 외부 모듈에서 해당 코드를 사용하려면, 해당 모듈의 `build.gradle.kts` 파일에 아래와 같이 의존성을 추가해야 합니다.

```kotlin
testApi(testFixtures(project(":domain")))
```

이 설정을 통해 외부 모듈에서는 domain 모듈의 `src/testFixtures`에 위치한 DomainFixture를 참조할 수 있게 됩니다. **이를 통해 각 모듈에서 중복되는 테스트 데이터 생성 코드를 줄이고, 각 모듈이 자체적으로 필요한 기능만 구현함으로써 외부 모듈의 부담을 덜 수 있습니다.** 한 번 작성된 테스트 데이터 생성 코드를 여러 모듈에서 재사용할 수 있으므로, 테스트 코드 작성이 크게 간편해집니다.

### 외부 인프라 의존으로 인한 Mocking 지옥 벗어나기

테스트 코드에서는 외부 서버에 이메일 전송을 위한 HTTP POST 요청을 Mocking해야 하므로, `ProductChangeNotificationRequest` 객체에 `ProductHistory`의 필드와 중복되는 부분이 12개 이상 포함되어 매번 모든 필드를 채워 넣어야 하는 문제가 발생합니다. 이로 인해 테스트 코드가 지나치게 복잡해지고, 실제로 테스트하고자 하는 핵심 관심사(예: 기존 히스토리 종료 및 신규 히스토리 생성)가 묻혀 버리는 문제가 있습니다.

이 문제를 해결하기 위해, DomainFixture에서 생성한 `ProductHistory` 객체를 기반으로 겹치는 필드를 자동으로 채우고, 이메일 전송에 필요한 필드만 별도로 지정할 수 있도록 `DomainIoFixture`의 `productChangeNotificationRequest` 메서드를 활용할 수 있습니다.

```kotlin
object DomainIoFixture {

    fun productChangeNotificationRequest(
        mailSubject: String = "제품 변경 알림",
        mailBody: String = "제품 정보가 변경되었습니다.",
        recipientEmail: String = "test@test.com",
        productHistory: ProductHistory
    ): ProductChangeNotificationRequest {
        return ProductChangeNotificationRequest(
            effectiveStartDate = productHistory.effectiveStartDate,
            effectiveEndDate = productHistory.effectiveEndDate,
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
            mailSubject = mailSubject,
            mailBody = mailBody,
            recipientEmail = recipientEmail,
        )
    }
}
```

`productChangeNotificationRequest`를 기반으로 작성한 테스트 코드는 다음과 같이 간결하게 표현할 수 있습니다.

```kotlin
@Test
fun `기존 히스토리종료 신규히스토리생성 정상동작 테스트`() {
    // given
    val productHistory = DomainFixture.productHistory(
        effectiveStartDate = LocalDate.of(2024, 1, 1),
        effectiveEndDate = LocalDate.of(2025, 1, 1),
    )
    persist(productHistory) // DB에 저장
    val request = DomainIoFixture.productChangeNotificationRequest(
        productHistory = productHistory,
        mailSubject = "Price Change Notification ...",
        mailBody = "Price of the product has been changed ...",
        recipientEmail = "sample@sample.test"
    )
    given(emailSender.sendProductChangeNotificationEmail(request)).willReturn(true)

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

이런 식으로 테스트 코드를 작성하면 불필요한 파라미터가 현저하게 줄어들며, 실제 요청 값과 동일한 값으로 Mocking 되기 때문에 테스트 실패 위험도를 줄일 수 있습니다. 그외에 필요한 필드만 명시적으로 지정합니다.

또한, 외부 통신 모듈을 여러 모듈에서 직접 의존하는 경우가 빈번하게 발생합니다. 예를 들어, 서비스 모듈의 특정 기능이 외부 서버와 통신해야 한다면, 그 서비스를 의존하는 API 모듈이나 배치 모듈도 결국 외부 통신 모듈을 직/간접적으로 의존하게 됩니다. 즉, API 모듈과 배치 모듈의 테스트 코드에서도 외부 서버와의 통신을 목킹해야 하는 상황이 발생하는데, 이때 `java-test-fixtures`를 활용해 테스트 전용 Mock Bean을 제공하면 외부 모듈에서도 손쉽게 테스트 코드를 작성할 수 있습니다.

예를 들어, 가맹점 정보를 조회하는 HTTP `PartnerClient` 클라이언트는 서비스 모듈, API 모듈, 배치 모듈 등에서 사용되기 때문에, 이를 재사용성을 극대화하여 제공하는 것이 좋습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/docs/image/image-3.png)

아래는 PartnerClient를 Mock 객체로 제공하는 예제입니다.

```kotlin
@TestConfiguration
class ClientTestConfiguration {

    @Bean
    @Primary
    fun mockPartnerClient() = mock(PartnerClient::class.java)!!
}
```

그리고 외부 모듈에서 해당 testFixtures 의존성을 추가하면, `/src/testFixtures/resources/META-INF/spring.factories` 파일에 다음과 같이 등록하여 자동으로 Bean이 주입되도록 할 수 있습니다.

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.spring.camp.io.ClientTestConfiguration
```

이와 같이 구성하면 `testApi(testFixtures(project(":http-client")))` 설정을 통해 외부 모듈에서도 Bean으로 등록된 Mock 객체를 의존성 주입받아 사용할 수 있습니다. 이를 통해, `PartnerClient`와 같이 여러 로직에 직간접적으로 의존하는 외부 통신 클라이언트를 보다 쉽게 Mocking하여 사용할 수 있습니다. 예를 들어, 다음과 같이 작성할 수 있습니다.

```kotlin
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class XXXServiceTest(
    private val mockPartnerClient: PartnerClient
) {

    @BeforeEach
    fun resetMock() {
        Mockito.reset(mockPartnerClient)
    }

    @Test
    fun `폐업 사업자 상태 변경 product을 판매 중지를 시킨다`() {
        // given
        val brn = "000-00-0000"
        given(mockPartnerClient.getPartnerStatus(brn))
            .willReturn(PartnerStatusResponse(status = PartnerStatus.OPEN, closeBusinessDate = null))

        // 해당 상태를 가진 사업자의 상품을 조회한다.
        // .. 기타 로직
    }
}
```

실제 Mock HTTP 서버를 띄우는 것보다, Mock 객체를 Bean으로 제공하는 방식이 훨씬 편리하며 테스트 코드를 작성하기 쉽게 만들어 줍니다. 이처럼 테스트 코드 작성이 용이해지면, 사업자가 휴업인 경우, 폐업인 경우, 일반인인 경우 등 다양한 케이스에 대한 테스트를 폭넓게 작성할 수 있어 전체 테스트 커버리지가 향상됩니다. 각각의 케이스에 대해 HTTP Mock 서버를 직접 띄우는 것은 번거로운 작업이지만, Mock 객체를 Bean으로 제공하는 방식을 사용하면 테스트 데이터 셋업이 간편해져 개발자가 다양한 상황에 대해 손쉽게 테스트 코드를 작성할 수 있습니다.

또한, 해당 서비스를 의존하는 web 모듈의 테스트 코드에서도 동일한 Mock 객체를 의존성 주입받아 사용할 수 있습니다. 예를 들어, 아래와 같이 작성할 수 있습니다.

```Kotlin
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class XXXControllerTest(
    private val mockMvc: MockMvc,
    private val mockPartnerClient: PartnerClient,
) {

    @BeforeEach
    fun resetMock() {
        Mockito.reset(mockPartnerClient)
    }

    @Test
    internal fun `xx 등록 API 테스트`() {
        //given
        val brn = "000-00-0000"
        given(mockPartnerClient.getPartnerStatus(brn))
            .willReturn(
                PartnerStatusResponse(
                    status = PartnerStatus.OPEN,
                    closeBusinessDate = null
                )
            )

        //when & then
        mockMvc.post("/v1/xxx") {
            contentType = MediaType.APPLICATION_JSON
            content = "..."
        }.andExpect {
            status { isOk() }
        }
    }
}
```

이처럼 `java-test-fixtures`를 활용하면 외부 통신 모듈의 Mock 객체를 여러 모듈에서 테스트 리소스로 공유하여 효율적으로 재사용할 수 있으며, 그 결과 테스트 코드 작성이 크게 간편해집니다.

요약하면, `DomainIoFixture`를 통해 `ProductHistory`와 중복되는 필드를 자동으로 채워 Given 절을 간결하게 구성하고, `java-test-fixtures`를 활용하여 외부 통신 모듈의 Mock 객체를 재사용하면, 복잡한 HTTP 요청 설정과 불필요한 목킹 코드로 인한 번거로움, 즉 Mocking 지옥에서 벗어나 효율적인 테스트 코드 작성이 가능합니다. 또한, `java-test-fixtures`를 통해 여러 모듈에서 테스트 리소스를 공유할 수 있으므로, 중복된 테스트 데이터 생성 코드 작성 부담이 줄어들고 각 모듈의 유지보수성이 크게 향상되는 이점도 누릴 수 있습니다.

> 이전 포스팅 "[테스트 코드의 피드백을 통한 Mock Server 테스트의 필요성 확인](https://tech.kakaopay.com/post/mock-test-code-part-2/#%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%BD%94%EB%93%9C%EC%9D%98-%ED%94%BC%EB%93%9C%EB%B0%B1%EC%9D%84-%ED%86%B5%ED%95%9C-mock-server-%ED%85%8C%EC%8A%A4%ED%8A%B8%EC%9D%98-%ED%95%84%EC%9A%94%EC%84%B1-%ED%99%95%EC%9D%B8)"에서는 HTTP Mock 서버 테스트와 행위 기반 Mocking 테스트의 관점에 대해 보다 자세히 다루었습니다.


## 스노우 볼을 굴려라

우리는 보통 작은 단위의 기능을 먼저 개발한 후, 이를 결합하여 더 큰 기능을 구현합니다. 테스트 코드 작성 역시 이와 비슷한 접근 방식이 필요합니다.
작은 기능의 테스트 코드를 작성할 때 사용한 Given 데이터를 재활용하여, 더 큰 기능의 테스트 코드를 작성하면 테스트 작성이 점차 수월해지고, 그에 따른 실질적인 이점을 체감할 수 있습니다. 이러한 이점이 있어야 테스트 코드 작성을 지속할 수 있는 원동력이 됩니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-camp-test/docs/image/image-4.png)

예를 들어, 상품 정보 변경 시 `ProductHistory`의 기존 히스토리를 종료하고 새로운 히스토리를 생성하여 변경된 내용을 반영하는 경우, `Product` 업데이트 로직, 데이터베이스 반영 로직, 기존 `ProductHistory` 조회 및 종료 로직, 신규 반영 내용 생성 로직 등 여러 기능을 순차적으로 작성하게 됩니다. 테스트 코드도 이와 같은 순서로 작성할 수 있습니다. 예를 들어, `Product`에 대한 데이터 셋업 메서드를 DomainFixture에 구현해두면, 이를 활용하여 `ProductHistory`에 대한 데이터 셋업 메서드를 보다 쉽게 작성할 수 있습니다. 또한, 이메일 전송 로직을 테스트할 때도 이전에 사용했던 Given 데이터를 재활용할 수 있으며, 해당 서비스 모듈을 호출하는 API 모듈이나 배치 모듈에서도 동일한 Given 절을 활용하여 테스트 코드를 간결하게 작성할 수 있습니다.
만약, 이메일 전송 실패 시 재시도 로직을 추가했다면, 해당 로직의 테스트 코드는 각 계층에 맞는 객체에서 진행할 수 있습니다. 이처럼, 작은 데이터 셋업부터 시작하여 점진적으로 스노우볼 효과를 거두는 방식은, 테스트 코드 작성의 부담을 줄이고 전체 테스트 커버리지를 향상시키는 핵심 전략입니다.

## 마치며

이번 포스팅에서는 테스트 코드 작성 시 발생하는 복잡한 Given 절 문제와, 이를 극복하기 위한 다양한 전략에 대해 살펴보았습니다. 작은 기능 단위의 테스트 데이터를 재활용하고, 모듈 간에 공유할 수 있는 방안을 통해 불필요한 중복과 번거로움을 줄임으로써, 테스트 코드의 가독성과 유지보수성을 크게 향상시킬 수 있음을 확인했습니다. 또한, 외부 인프라 의존으로 인한 Mocking 문제를 효과적으로 처리하여, 테스트 코드 작성이 보다 효율적이고 실질적인 이점을 제공하는 방향으로 나아갈 수 있음을 보여드렸습니다.
이러한 전략들이 지속적으로 적용된다면, 테스트 코드가 점차 스노우볼처럼 커져 전체 테스트 커버리지와 시스템의 신뢰성을 높이는 데 큰 도움이 될 것입니다. 앞으로도 이와 같은 방법들을 통해 보다 체계적이고 효과적인 테스트 코드 작성 문화를 만들어 나가시길 바랍니다.
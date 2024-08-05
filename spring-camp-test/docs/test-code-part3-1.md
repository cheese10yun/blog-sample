# 실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 3: Given 지옥에서 벗어나기 - 객체 기반 데이터 셋업의 한계

지난 시리즈에서는 Mock Test를 효율적으로 작성하는 방법과 테스트 코드로부터 피드백을 받는 방법에 대해 다루었습니다. [Part 1: 효율적인 Mock Test](https://tech.kakaopay.com/post/mock-test-code/)에서는 Mock Test의 중요성과 작성 방법에 대해 설명했으며, [Part 2: 테스트 코드로부터 피드백 받기](https://tech.kakaopay.com/post/mock-test-code-part-2/)에서는 테스트 코드를 통해 얻을 수 있는 인사이트와 개선 방법을 소개했습니다.

이번 글에서는 테스트 코드 작성 시 자주 겪게 되는 Given 단계에서의 어려움을 극복하는 방법에 대해 다루어 보겠습니다. Given 단계는 테스트의 준비 단계로서, 이 단계에서 복잡한 데이터 셋업이 자주 요구되며, 이로 인해 다양한 테스트 코드를 작성하기 어려워지고, 결국 폭넓은 테스트 커버리지를 확보하기 힘들어집니다. 이러한 문제를 해결하기 위한 전략과 실무에서 활용할 수 있는 팁들을 소개하겠습니다. 특히 객체 기반 테스트에서 Given 데이터 셋업의 한계와 이를 극복하는 방법에 대해 자세히 설명할 것입니다.

## Given 단계의 미로: 객체 기반 테스트의 어려움과 해결책

![](/images/part3/order.png)

실무에서 주문과 관련된 테스트 코드를 작성할 때, 다양한 데이터를 여러 케이스에 맞게 셋업해야 하는 상황을 자주 맞닥뜨리게 됩니다. 예를 들어, 할인 쿠폰을 적용할 때는 쿠폰의 할인율, 적용 가능한 제품, 업체와의 쿠폰 분담 비율 등 여러 가지 변수를 고려해야 합니다. 이러한 다양한 변수들이 얽히고설켜 복잡한 데이터 셋업이 필요하게 됩니다.

주문 시스템은 상품, 회원, 쿠폰, 결제 정보 등 여러 요소가 결합된 복잡한 구조로 이루어져 있습니다. 각 요소들이 상호작용하며 다양한 시나리오가 만들어지기 때문에, 모든 경우를 테스트하기 위해서는 각기 다른 데이터 셋업이 필수적입니다. 이러한 작업은 매우 번거롭고 시간이 많이 소요되며, 모든 경우를 놓치지 않기 위해서는 꼼꼼한 준비가 필요합니다.

특히, 객체 기반의 데이터 셋업을 통해 테스트 코드를 작성할 때 이러한 어려움은 더욱 커집니다. 객체 기반 테스트에서는 데이터 구조가 복잡해지고 각 객체 간의 의존성을 직접 설정해야 하기 때문에, 테스트 코드 작성이 매우 어려워질 수 있습니다. 이는 결국 개발자들에게 큰 부담이 되며, 다양한 시나리오를 충분히 테스트하기 위해 효율적으로 데이터를 준비하는 데 어려움을 겪게 만듭니다.

이러한 어려움을 극복하기 위해 이번 글에서는 복잡한 데이터 셋업을 보다 효과적으로 관리할 수 있는 전략과 실무에서 활용할 수 있는 팁을 공유하고자 합니다.

### 다양한 데이터 셋업이 필요한 경우



## Given 단계의 어려움을 극복하는 방법

테스트 코드를 작성하는 것은 소프트웨어 개발에서 중요한 부분입니다. 하지만 테스트 코드 작성 시 종종 겪는 문제는 데이터 셋업의 복잡성입니다. 특히, 객체 기반의 데이터 셋업 방식과 비교하여 JSON 파일을 이용한 데이터 셋업의 장점에 대해 이야기해 보겠습니다.

![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.131.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.132.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.133.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.134.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.135.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.136.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.137.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.138.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.139.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.140.jpeg)

![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.141.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.142.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.143.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.144.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.145.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.146.jpeg)

### 객체 기반 데이터 셋업의 문제점 1

### 기존의 객체 기반 방식

```kotlin
@Test
internal fun `주문 API TEST`() {
    // given
    val dto = OrderRequest(
        orderNumber = "A00001",
        status = "READY",
        price = 1000L,
        address = Address(
            zipCode = "023",
            address = "서울 중구 을지로 65",
            detail = "SK텔레콤빌딩"
        )
    )

    val requestBody = objectMapper.writeValueAsString(dto)

    // when & then
    mockMvc.post("/v1/orders") {
        contentType = MediaType.APPLICATION_JSON
        content = requestBody
    }.andExpect {
        status { isOk() }
    }
}
```

1. **복잡한 구조**: API 테스트를 위해 객체를 직접 생성해야 하는 경우, 데이터 구조가 복잡해질 수 있습니다. 특히, 객체 내에 다른 객체나 배열이 포함된 경우, 이를 일일이 셋업하는 과정은 번거롭습니다.
2. **유연성 부족**: 객체 기반 셋업은 코드 변경 시 테스트 코드도 함께 수정해야 하는 번거로움이 있습니다. 또한, 다양한 케이스를 테스트하기 위해 객체를 생성하고 수정하는 과정이 비효율적입니다.

위 예제에서 보듯이, `OrderRequest` 객체를 생성하고, 이를 JSON으로 변환하여 API 요청을 만듭니다.

### JSON 파일을 이용한 데이터 셋업의 장점

1. **직관적인 구조**: JSON 형식은 데이터 구조가 직관적이어서 쉽게 이해할 수 있습니다. 복잡한 객체 구조도 JSON 파일로 표현하면 더 명확해집니다.
2. **유연성**: JSON 파일을 사용하면 다양한 테스트 케이스를 쉽게 추가하고 수정할 수 있습니다. 코드 수정 없이도 JSON 파일만 교체하여 다양한 데이터를 테스트할 수 있습니다.
3. **재사용성**: 동일한 JSON 파일을 여러 테스트에서 재사용할 수 있습니다. 이는 테스트 코드의 중복을 줄이고, 유지 보수를 용이하게 합니다.

다음은 JSON 파일을 이용한 셋업의 예시입니다:

```json
{
  "order_number": "A00001",
  "status": "READY",
  "price": 1000,
  "address": {
    "zip_code": "023",
    "address": "서울 중구 을지로 65",
    "detail": "SK텔레콤빌딩 4층 수펙스홀"
  },
  "products": [
    {
      "name": "에어 조던 XXXVII 로우 PF",
      "size": "230",
      "tags": [
        "신발",
        "나이키",
        "에어 조던"
      ]
    },
    {
      "name": "나이키 에어맥스 1 '86 OG G",
      "size": "240",
      "tags": [
        "신발",
        "나이키",
        "에어맥스"
      ]
    }
  ]
}
```

위 JSON 파일을 테스트 코드에서 사용하는 방법은 다음과 같습니다:

```kotlin
@Test
internal fun `주문 API TEST`() {
    // given
    val requestBody = Files.readString(Paths.get("src/test/resources/order_request.json"))

    // when & then
    mockMvc.post("/v1/orders") {
        contentType = MediaType.APPLICATION_JSON
        content = requestBody
    }.andExpect {
        status { isOk() }
    }
}
```

### 주요 관심사: 테스트의 더 중요한 가치

테스트 코드는 JSON을 입력받아 이후 로직 검증을 주요 관심사로 삼아야 합니다. 특정 객체를 Deserialize하는 것은 주요 관심사가 아닙니다.

다음은 기존의 객체 기반 방식과 JSON 파일 기반 방식을 비교한 예시입니다:

### JSON 파일을 이용한 방식

```kotlin
@Test
internal fun `주문 API TEST`() {
    // given
    val requestBody = readJson("/order-1.json")

    // when & then
    mockMvc.post("/v1/orders") {
        contentType = MediaType.APPLICATION_JSON
        content = requestBody
    }.andExpect {
        status { isOk() }
    }
}

private fun readJson(path: String): String {
    return this::class.java.getResource(path).readText(Charsets.UTF_8)
}
```

## 문제와 해결

JSON 파일을 활용한 데이터 셋업은 객체 기반 셋업의 복잡성을 줄이고, 유지 보수성을 향상시킵니다. 이는 다양한 테스트 케이스를 쉽게 추가하고 수정할 수 있게 하여, 테스트 코드의 품질을 높입니다.

- **address 필드 추가로 인한 작업**: JSON 파일을 통해 쉽게 해결 가능
- **products 필드 추가**: JSON 배열로 간단하게 표현
- **tags 필드 추가**: 배열의 배열 구조도 쉽게 표현 가능
- **CamelCase 형식**: JSON에서 그대로 사용 가능
- **status 필드의 Enum 값 검증**: JSON에서 다양한 값을 테스트 가능
- **nullable 필드**: JSON에서 필드 생략하여 테스트 가능

JSON 파일을 이용한 데이터 셋업은 테스트 코드의 유연성과 재사용성을 높여줍니다. 이를 통해 더 효과적이고 유지 보수하기 쉬운 테스트 코드를 작성할 수 있습니다.

### 객체 기반 데이터 셋업의 문제점 2

![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.147.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.148.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.149.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.150.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.151.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.152.jpeg)
![](/images/part3/실무에서%20적용하는%20테스트%20코드%20작성%20방법과%20노하우-작업용.153.jpeg)

객체 기반 데이터 셋업은 테스트 코드 작성 시 여러 가지 문제점을 야기할 수 있습니다. 특히 상태 변경이 빈번한 시스템에서는 특정 상태를 쉽게 셋업하는 것이 어려워집니다.

#### 복잡한 구조

API 테스트를 위해 객체를 직접 생성해야 하는 경우, 데이터 구조가 복잡해질 수 있습니다. 특히, 객체 내에 다른 객체나 배열이 포함된 경우, 이를 일일이 셋업하는 과정은 번거롭습니다.

```kotlin
@Test
fun `상품 준비중 to 배송 시작 status 변경 테스트`() {
    // given
    val orderNumber = "order-number-123"
    val order = Order(orderNumber)
    order.updateStatusCompletePayment()
    order.updateStatusProductPreparation()

    // 데이터 셋업 완료
    order.updateStatusDeliveryStarted()
}
```

위와 같은 객체 기반 상태 변경 방식은 복잡하고 유지보수가 어렵습니다.

#### 유연성 부족

객체 기반 셋업은 코드 변경 시 테스트 코드도 함께 수정해야 하는 번거로움이 있습니다. 또한, 다양한 케이스를 테스트하기 위해 객체를 생성하고 수정하는 과정이 비효율적입니다.

```kotlin
@Test
fun `상품 준비중 to 배송 시작 status 변경 테스트`() {
    // given
    val orderNumber = "order-number-123"
    val order = Order(orderNumber)
    order.updateStatusCompletePayment()
    order.updateStatusProductPreparation()

    // 데이터 셋업 완료
    order.updateStatusDeliveryStarted()
}
```

이와 같은 방식은 특정 시점의 상태로 객체를 셋업하는 것이 어렵고, 다양한 테스트 케이스를 작성하는 데 제한이 됩니다.

### SQL 파일을 이용한 데이터 셋업의 장점

SQL 파일을 이용한 데이터 셋업은 이러한 문제를 해결할 수 있는 유용한 방법입니다.

#### 직관적인 데이터 셋업

SQL 파일을 사용하면 특정 상태의 데이터를 직접 셋업할 수 있어 코드가 간결해집니다.

```sql
-- order-setup.sql
insert into orders (order_number, status)
values ('order-number-1', 'PRODUCT_PREPARATION');
```

#### 유연성

다양한 상태의 데이터를 손쉽게 생성할 수 있어 폭넓은 테스트 케이스를 작성할 수 있습니다.

```kotlin
@Test
@Sql("/order-setup.sql")
fun `상품 준비중 to 배송 시작 status 변경 테스트`() {
    // given
    val order = orderRepository.findAll().first()

    // when
    order.updateStatusDeliveryStarted()

    // then
    assertThat(order.status).isEqualTo(OrderStatus.DELIVERY_STARTED)
}
```

#### 독립성

SQL 파일을 사용하면 테스트 데이터가 독립적으로 관리되어 다른 테스트에 영향을 미치지 않습니다.

```sql
-- order-setup.sql
insert into orders (order_number, status)
values ('order-number-1', 'PRODUCT_PREPARATION');
```

이렇게 SQL 파일을 사용하여 데이터베이스 상태를 미리 셋업하면, 객체 기반의 복잡한 상태 변경 로직을 단순화할 수 있습니다. 이는 테스트 코드의 가독성을 높이고, 유지보수를 쉽게 만들어줍니다.

### 결론

setter가 없는 경우 특정 시점으로 데이터 셋업이 어렵기 때문에, SQL 기반으로 데이터 셋업을 하면 보다 쉽게 데이터 셋업을 할 수 있습니다. 이를 통해 다양한 데이터 셋업이 가능하고, 폭넓은 테스트 코드를 작성할 수 있습니다. SQL 파일을 활용하면 특정 상태로 데이터를 셋업할 수 있어 테스트 코드의 유연성과 재사용성을 높일 수 있습니다. 이를 통해 더 효과적이고 유지보수하기 쉬운 테스트 코드를 작성할 수 있습니다.

## 마무리

- 복잡한 테스트 코드의 Given 작성은 테스트 코드 작성의 어려움을 가중시키는 요인, 결과적으로 테스트 코드를 작성하지 않게 됨
- 각 상황에 맞게 Given 절을 효율적으로 작성하는 방법에 대해서 지속적으로 고민하고 개선해야 함
- 내가 작성한 테스트 코드가 다른 테스트 코드에도 도움을 주기 때문에 어느정도 테스트 코드를 작성하면 스노우볼을 굴리듯이 폭넓은 테스트 코드가 작성되는 선순환 구조를 갖게 됨
- 우리가 작성하는 기능 코드 처럼 테스트 코드도 관심을 갖고 계속 발전시켜야 한다.
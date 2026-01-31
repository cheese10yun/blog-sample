# Kotlin으로 구현하는 필드 단위 변경 이력 추적 시스템

## 들어가며

운영 환경에서 데이터 변경 이력을 추적해야 하는 경우가 자주 발생합니다. 특히 주문 정보 수정, 가맹점 수수료율 변경 등 중요한 데이터가 어떻게 변경되었는지 필드 단위로 명확하게 기록하고 확인할 수 있어야 합니다. 

예를 들어, 운영자가 주문 내역에서 배송 주소만 변경했을 때, 전체 주문 데이터를 다시 저장하는 것보다 "어떤 필드가 어떻게 변경되었는지"를 명확히 기록하면 다음과 같은 이점이 있습니다:

- 변경 이력 추적이 명확해집니다
- 승인 프로세스에서 변경 내용 검토가 용이합니다
- 디버깅 및 감사(Audit) 목적으로 활용할 수 있습니다
- 데이터 롤백 시 정확한 변경 지점을 파악할 수 있습니다

이번 포스트에서는 Kotlin과 Jackson을 활용하여 복잡한 중첩 객체의 변경사항을 자동으로 추적하는 시스템을 구현하는 방법을 알아보겠습니다.

## 문제 상황

다음과 같은 주문(Order) 데이터가 있다고 가정해봅시다.

### 변경 전 데이터
```json
{
  "order_id": "ORD123456",
  "customer": {
    "customer_id": "CUST7890",
    "name": "홍길동",
    "contact": {
      "email": "hong@example.com",
      "phone": "010-1234-5678",
      "address": {
        "street": "서울특별시 종로구",
        "city": "서울",
        "zip_code": "03000",
        "country": "KR"
      }
    }
  },
  "items": [
    {
      "product": {
        "product_id": "PROD001",
        "product_name": "노트북",
        "category": {
          "main_category": "전자제품",
          "sub_category": "컴퓨터"
        }
      },
      "quantity": 1,
      "price": 1500000
    }
  ],
  "payment": {
    "method": "신용카드",
    "transaction_id": "TXN987654321",
    "status": "완료"
  }
}
```

### 변경 후 데이터
```json
{
  "order_id": "ORD123456",
  "customer": {
    "customer_id": "CUST7890",
    "name": "홍길동",
    "contact": {
      "email": "hong@example.com",
      "phone": "010-1234-5678",
      "address": {
        "street": "서울특별시 강남구",
        "city": "서울",
        "zip_code": "03000",
        "country": "KR"
      }
    }
  },
  "items": [
    {
      "product": {
        "product_id": "PROD001",
        "product_name": "노트북",
        "category": {
          "main_category": "전자제품",
          "sub_category": "컴퓨터"
        }
      },
      "quantity": 1,
      "price": 1400000
    }
  ],
  "payment": {
    "method": "신용카드",
    "transaction_id": "TXN987654322",
    "status": "완료"
  }
}
```

위 두 데이터를 비교하면 다음 필드들이 변경되었습니다:
- `customer.contact.address.street`: "서울특별시 종로구" → "서울특별시 강남구"
- `items[0].price`: 1500000 → 1400000
- `payment.transaction_id`: "TXN987654321" → "TXN987654322"

이러한 변경사항을 자동으로 감지하고 추적하려면 어떻게 해야 할까요?

## IntelliJ의 Diff 기능처럼

IntelliJ IDE를 사용해보신 분들은 아시겠지만, 두 파일을 비교할 때 매우 직관적으로 변경 사항을 표시해줍니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/refs/heads/master/boot3-mongo/docs/json-diff-1.png)

우리가 구현하려는 시스템도 이와 유사하게 두 객체를 비교하여 변경된 필드만 추출하는 것입니다.

## 구현 방법

### 1. 핵심 라이브러리: zjsonpatch

JSON 객체 간의 차이를 계산하기 위해 `zjsonpatch` 라이브러리를 사용합니다. 이 라이브러리는 RFC 6902 JSON Patch 표준을 구현하여 두 JSON 문서의 차이를 효과적으로 계산합니다.

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.flipkart.zjsonpatch:zjsonpatch:0.4.14")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```

### 2. DiffComparisonManager 구현

전체 코드는 다음과 같습니다:

```kotlin
package com.example.boot3mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import org.bson.types.ObjectId

typealias DiffValueTracker = Map<String, DiffValue<String, String>>
typealias DiffTriple = Triple<String, String, String>

object DiffComparisonManager {

    private val diffMapper = jacksonObjectMapper()
        .apply {
            registerModules(
                SimpleModule().apply {
                    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                    addSerializer(ObjectId::class.java, ObjectIdSerializer())
                }
            )
        }

    fun <T> calculateDifference(
        originItem: T,
        newItem: T
    ): DiffValueTracker {
        val originalNode = diffMapper.valueToTree<JsonNode>(originItem)
        val newNode = diffMapper.valueToTree<JsonNode>(newItem)
        val diff = JsonDiff.asJson(originalNode, newNode)
        return when {
            diff.size() > 0 -> {
                diff.mapNotNull { diffNode ->
                    val (path, originValue, newValue) = extractDiffValue(diffNode, originalNode, newNode)
                    Pair(
                        first = path,
                        second = DiffValue(originValue, newValue)
                    )
                }
                    .toMap()
            }
            else -> emptyMap()
        }
    }

    fun <T, K, S> calculateDifferences(
        originItems: List<T>,
        newItems: List<T>,
        associateByKey: (T) -> K,
        groupByKey: (T) -> S
    ): Map<S, DiffValueTracker> {
        val originalAssociate = originItems.associateBy(associateByKey)
        val newAssociate = newItems.associateBy(associateByKey)
        val changes = newAssociate.flatMap { (id, newItem) ->
            val originalItem = originalAssociate[id]
            when {
                originalItem != null -> {
                    val originalNode = diffMapper.valueToTree<JsonNode>(originalItem)
                    val newNode = diffMapper.valueToTree<JsonNode>(newItem)
                    val diffNode = JsonDiff.asJson(originalNode, newNode)

                    when {
                        diffNode.size() > 0 -> {
                            diffNode.mapNotNull { node ->
                                val (path, originValue, newValue) = extractDiffValue(node, originalNode, newNode)

                                Triple(
                                    first = groupByKey(newItem),
                                    second = path,
                                    third = DiffValue(origin = originValue, new = newValue)
                                )
                            }
                        }
                        else -> emptyList()
                    }
                }
                else -> emptyList()
            }
        }

        return changes
            .groupBy({ it.first }, { it.second to it.third })
            .mapValues { (_, value) -> value.toMap() }
    }

    private fun extractDiffValue(node: JsonNode, originalNode: JsonNode, newNode: JsonNode): DiffTriple {
        val path = node.get("path").asText().removePrefix("/")
        val originValue = originalNode.at("/$path").asText()
        val newValue = newNode.at("/$path").asText()
        return DiffTriple(path, originValue, newValue)
    }
}

data class DiffValue<out A, out B>(
    val origin: A,
    val new: B
)

class ObjectIdSerializer : JsonSerializer<ObjectId>() {
    override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}
```

### 3. 코드 상세 설명

#### 3.1 Jackson ObjectMapper 설정

```kotlin
private val diffMapper = jacksonObjectMapper()
    .apply {
        registerModules(
            SimpleModule().apply {
                propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                addSerializer(ObjectId::class.java, ObjectIdSerializer())
            }
        )
    }
```

- **Snake Case 변환**: Kotlin의 camelCase 필드명을 JSON의 snake_case로 자동 변환합니다
- **ObjectId 직렬화**: MongoDB의 ObjectId를 문자열로 변환하는 커스텀 Serializer를 등록합니다
- 이를 통해 `productName` → `product_name`으로 자동 변환되어 일관된 필드명으로 추적할 수 있습니다

#### 3.2 calculateDifference 함수

단일 객체 간의 차이를 계산하는 핵심 함수입니다.

```kotlin
fun <T> calculateDifference(
    originItem: T,
    newItem: T
): DiffValueTracker {
    // 1. Kotlin 객체를 JsonNode로 변환
    val originalNode = diffMapper.valueToTree<JsonNode>(originItem)
    val newNode = diffMapper.valueToTree<JsonNode>(newItem)
    
    // 2. JsonDiff로 차이 계산
    val diff = JsonDiff.asJson(originalNode, newNode)
    
    // 3. 차이가 있으면 변경 정보 추출
    return when {
        diff.size() > 0 -> {
            diff.mapNotNull { diffNode ->
                val (path, originValue, newValue) = extractDiffValue(diffNode, originalNode, newNode)
                Pair(
                    first = path,
                    second = DiffValue(originValue, newValue)
                )
            }.toMap()
        }
        else -> emptyMap()
    }
}
```

**동작 과정:**

1. **객체를 JsonNode로 변환**: Kotlin 객체를 Jackson의 JsonNode로 변환하여 JSON 구조로 다룰 수 있게 합니다
2. **JsonDiff 계산**: `JsonDiff.asJson()`을 사용하여 두 JsonNode 간의 차이를 계산합니다
3. **변경 정보 추출**: 각 diff node에서 경로(path), 이전 값(originValue), 새 값(newValue)을 추출합니다
4. **결과 반환**: `Map<String, DiffValue>` 형태로 반환합니다
   - Key: 필드 경로 (예: `customer/contact/address/street`)
   - Value: `DiffValue(origin, new)` 객체

#### 3.3 extractDiffValue 함수

```kotlin
private fun extractDiffValue(node: JsonNode, originalNode: JsonNode, newNode: JsonNode): DiffTriple {
    val path = node.get("path").asText().removePrefix("/")
    val originValue = originalNode.at("/$path").asText()
    val newValue = newNode.at("/$path").asText()
    return DiffTriple(path, originValue, newValue)
}
```

- **path 추출**: diff node에서 변경된 필드의 경로를 추출합니다 (예: `/customer/contact/address/street`)
- **슬래시 제거**: 경로 앞의 `/`를 제거하여 깔끔한 key로 만듭니다
- **값 추출**: JsonNode의 `at()` 메서드로 해당 경로의 값을 추출합니다
- **Triple 반환**: (경로, 이전값, 새값)을 하나의 Triple로 반환합니다

#### 3.4 calculateDifferences 함수 (복수 객체 처리)

여러 객체를 비교할 때 사용하는 함수입니다.

```kotlin
fun <T, K, S> calculateDifferences(
    originItems: List<T>,
    newItems: List<T>,
    associateByKey: (T) -> K,      // 매칭용 키 (예: ID)
    groupByKey: (T) -> S            // 그룹화용 키
): Map<S, DiffValueTracker>
```

- **associateByKey**: 원본과 새로운 데이터를 매칭하기 위한 키 (예: orderId, productId)
- **groupByKey**: 결과를 그룹화하기 위한 키
- 여러 객체를 한 번에 처리하고 각 객체별 변경사항을 그룹화하여 반환합니다

## 테스트 코드로 검증하기

다양한 케이스를 테스트하여 구현이 올바르게 동작하는지 확인했습니다.

### 1. 단일 필드 변경 감지

```kotlin
@Test
fun `calculateDifference - 단일 객체의 변경 사항을 감지한다`() {
    // Given
    val originalProduct = Product(
        productId = "PROD001",
        productName = "노트북",
        category = Category("전자제품", "컴퓨터")
    )
    val newProduct = Product(
        productId = "PROD001",
        productName = "울트라 노트북",
        category = Category("전자제품", "컴퓨터")
    )

    // When
    val result = DiffComparisonManager.calculateDifference(originalProduct, newProduct)

    // Then
    then(result).hasSize(1)
    then(result["product_name"]).isNotNull
    then(result["product_name"]?.origin).isEqualTo("노트북")
    then(result["product_name"]?.new).isEqualTo("울트라 노트북")
}
```

**결과:**
```
{
  "product_name": {
    "origin": "노트북",
    "new": "울트라 노트북"
  }
}
```

### 2. 중첩된 객체의 변경 감지

```kotlin
@Test
fun `calculateDifference - 중첩된 객체의 변경 사항을 감지한다`() {
    // Given
    val originalProduct = Product(
        productId = "PROD001",
        productName = "노트북",
        category = Category("전자제품", "컴퓨터")
    )
    val newProduct = Product(
        productId = "PROD001",
        productName = "노트북",
        category = Category("전자제품", "노트북")
    )

    // When
    val result = DiffComparisonManager.calculateDifference(originalProduct, newProduct)

    // Then
    then(result).hasSize(1)
    then(result["category/sub_category"]).isNotNull
    then(result["category/sub_category"]?.origin).isEqualTo("컴퓨터")
    then(result["category/sub_category"]?.new).isEqualTo("노트북")
}
```

**결과:** 중첩 객체의 필드도 `category/sub_category` 형태로 경로가 명확히 표시됩니다.

### 3. 여러 필드 동시 변경 감지

```kotlin
@Test
fun `calculateDifference - 여러 필드의 변경 사항을 감지한다`() {
    // Given
    val originalItem = Item(
        product = Product("PROD001", "노트북", Category("전자제품", "컴퓨터")),
        quantity = 2,
        price = 1500000
    )
    val newItem = Item(
        product = Product("PROD001", "울트라 노트북", Category("전자제품", "컴퓨터")),
        quantity = 3,
        price = 1400000
    )

    // When
    val result = DiffComparisonManager.calculateDifference(originalItem, newItem)

    // Then
    then(result).hasSize(3)
    then(result["product/product_name"]?.origin).isEqualTo("노트북")
    then(result["product/product_name"]?.new).isEqualTo("울트라 노트북")
    then(result["quantity"]?.origin).isEqualTo("2")
    then(result["quantity"]?.new).isEqualTo("3")
    then(result["price"]?.origin).isEqualTo("1500000")
    then(result["price"]?.new).isEqualTo("1400000")
}
```

**결과:**
```
{
  "product/product_name": { "origin": "노트북", "new": "울트라 노트북" },
  "quantity": { "origin": "2", "new": "3" },
  "price": { "origin": "1500000", "new": "1400000" }
}
```

### 4. 깊은 중첩 구조 변경 감지

```kotlin
@Test
fun `calculateDifference - 깊게 중첩된 객체의 변경 사항을 감지한다`() {
    // Given
    val originalOrder = Order(
        orderId = "ORD123",
        customer = Customer(
            customerId = "CUST001",
            name = "홍길동",
            contact = Contact(
                email = "hong@example.com",
                phone = "010-1234-5678",
                address = Address("서울특별시 종로구", "서울", "03001", "대한민국")
            )
        ),
        items = emptyList(),
        payment = Payment("신용카드", "TXN001", "완료")
    )
    val newOrder = Order(
        orderId = "ORD123",
        customer = Customer(
            customerId = "CUST001",
            name = "홍길동",
            contact = Contact(
                email = "hong@example.com",
                phone = "010-1234-5678",
                address = Address("서울특별시 강남구", "서울", "06001", "대한민국")
            )
        ),
        items = emptyList(),
        payment = Payment("신용카드", "TXN001", "완료")
    )

    // When
    val result = DiffComparisonManager.calculateDifference(originalOrder, newOrder)

    // Then
    then(result).hasSize(2)
    then(result["customer/contact/address/street"]?.origin).isEqualTo("서울특별시 종로구")
    then(result["customer/contact/address/street"]?.new).isEqualTo("서울특별시 강남구")
    then(result["customer/contact/address/zip_code"]?.origin).isEqualTo("03001")
    then(result["customer/contact/address/zip_code"]?.new).isEqualTo("06001")
}
```

**결과:** 4단계 깊이의 중첩 구조(`customer/contact/address/street`)도 정확히 추적합니다.

### 5. Null 처리

```kotlin
@Test
fun `calculateDifference - null에서 값으로 변경을 감지한다`() {
    // Given
    data class TestData(val name: String, val description: String?)
    val original = TestData("테스트", null)
    val new = TestData("테스트", "설명 추가")

    // When
    val result = DiffComparisonManager.calculateDifference(original, new)

    // Then
    then(result).hasSize(1)
    then(result["description"]?.origin).isEmpty()
    then(result["description"]?.new).isEqualTo("설명 추가")
}

@Test
fun `calculateDifference - 값에서 null로 변경을 감지한다`() {
    // Given
    data class TestData(val name: String, val description: String?)
    val original = TestData("테스트", "기존 설명")
    val new = TestData("테스트", null)

    // When
    val result = DiffComparisonManager.calculateDifference(original, new)

    // Then
    then(result).hasSize(1)
    then(result["description"]?.origin).isEqualTo("기존 설명")
    then(result["description"]?.new).isEmpty()
}
```

### 6. 변경 없는 경우

```kotlin
@Test
fun `calculateDifference - 동일한 객체는 변경 사항이 없다`() {
    // Given
    val product = Product(
        productId = "PROD001",
        productName = "노트북",
        category = Category("전자제품", "컴퓨터")
    )

    // When
    val result = DiffComparisonManager.calculateDifference(product, product)

    // Then
    then(result).isEmpty()
}
```

### 7. 실제 주문 데이터 변경 추적

```kotlin
@Test
fun `주문 데이터의 필드 변경을 확인한다`() {
    // Given
    val originalOrder: Order = diffMapper.readValue(readFile("/diff-origin.json"))
    val newOrder: Order = diffMapper.readValue(readFile("/diff-new.json"))

    // When
    val result = DiffComparisonManager.calculateDifferences(
        originItems = listOf(originalOrder),
        newItems = listOf(newOrder),
        associateByKey = { it.orderId },
        groupByKey = { it.orderId }
    )

    // Then
    val differences = result["ORD123456"]
    then(differences).isNotNull
    then(differences!!.size).isEqualTo(3)
    then(differences["customer/contact/address/street"]?.origin).isEqualTo("서울특별시 종로구")
    then(differences["customer/contact/address/street"]?.new).isEqualTo("서울특별시 강남구")
    then(differences["items/0/price"]?.origin).isEqualTo("1500000")
    then(differences["items/0/price"]?.new).isEqualTo("1400000")
    then(differences["payment/transaction_id"]?.origin).isEqualTo("TXN987654321")
    then(differences["payment/transaction_id"]?.new).isEqualTo("TXN987654322")
}
```

## 활용 방안

### 1. 승인 프로세스에 활용

```kotlin
data class ApprovalRequest(
    @Id
    val id: ObjectId = ObjectId(),
    val requestType: String,
    val targetId: String,
    val changes: DiffValueTracker,       // 어떤 필드가 어떻게 변경될지
    val requestedBy: String,
    val status: ApprovalStatus = ApprovalStatus.PENDING
)

// 승인 요청 생성
fun createFeeChangeApproval(merchantId: String, currentFee: MerchantFee, newFee: MerchantFee, userId: String) {
    val changes = DiffComparisonManager.calculateDifference(currentFee, newFee)
    
    val approvalRequest = ApprovalRequest(
        requestType = "MERCHANT_FEE_CHANGE",
        targetId = merchantId,
        changes = changes,
        requestedBy = userId
    )
    approvalRequestRepository.save(approvalRequest)
    
    // 승인권자에게 알림 전송
    notifyApprovers(approvalRequest)
}
```

### 2. 감사 로그 및 모니터링

```kotlin
// 중요 필드 변경 모니터링
fun monitorCriticalChanges(changes: DiffValueTracker, entityType: String) {
    val criticalFields = setOf(
        "payment/method",
        "customer/contact/address/street",
        "items/0/price"
    )
    
    changes.keys.filter { it in criticalFields }
        .forEach { field ->
            val change = changes[field]!!
            logger.warn(
                "Critical field changed in $entityType: $field - " +
                "from '${change.origin}' to '${change.new}'"
            )
            // 알림 전송, 메트릭 기록 등
        }
}
```

## 장점과 고려사항

### 장점

1. **자동화**: 수동으로 변경 필드를 비교할 필요 없이 자동으로 추적합니다
2. **타입 안정성**: Kotlin의 제네릭을 활용하여 타입 안전하게 구현됩니다
3. **중첩 객체 지원**: 깊은 중첩 구조도 경로로 명확히 표시합니다
4. **저장소 독립성**: 특정 데이터베이스에 의존하지 않는 순수한 로직으로 구현되어, MongoDB, PostgreSQL, MySQL 등 어떤 저장소에도 저장할 수 있습니다
5. **가독성**: 변경 내역이 명확한 key-value 형태로 저장됩니다

### 고려사항

1. **성능**: 큰 객체나 대량의 데이터를 비교할 때는 성능을 고려해야 합니다
2. **배열 처리**: 배열의 순서가 바뀌면 전체가 변경된 것으로 인식될 수 있습니다
3. **저장 공간**: 모든 변경 이력을 저장하면 데이터가 빠르게 증가할 수 있습니다
4. **민감 정보**: 비밀번호 등 민감한 정보는 이력에서 제외하는 로직이 필요합니다

## 마치며

이번 포스트에서는 Kotlin과 Jackson, zjsonpatch를 활용하여 필드 단위 변경 이력 추적 시스템을 구현하는 방법을 알아보았습니다. 

복잡한 중첩 객체의 변경사항을 자동으로 추적하고 명확한 경로로 표시하는 이 시스템은 다음과 같은 상황에서 유용하게 활용할 수 있습니다:

- 주문/결제 정보 변경 이력 추적
- 가맹점 정보 변경 승인 프로세스
- 감사(Audit) 로그 시스템
- 데이터 동기화 및 충돌 감지

실제 프로덕션 환경에 적용할 때는 성능과 저장 공간, 민감 정보 처리 등을 충분히 고려하여 상황에 맞게 커스터마이징하시기 바랍니다.

전체 코드는 [GitHub Repository](#)에서 확인하실 수 있습니다.

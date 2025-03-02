# Spring Data MongoDB로 배열의 특정 요소 업데이트하기

Spring Data MongoDB를 활용해 한 도큐먼트(한 row)의 특정 배열 요소만 선택적으로 업데이트하는 방법을 알아보겠습니다.

일반적으로 find로 데이터를 조회한 후 save로 업데이트하는 방식은 편리하지만, **대량 데이터를 처리할 때는 updateOne 또는 updateMany를 이용한 벌크 업데이트가 성능 면에서 큰 이점을 제공합니다.**  
이번 포스팅에서는 MongoDB의 arrayFilters 옵션과 Spring Data MongoDB를 사용해 배열의 특정 요소만 업데이트하는 방법을 살펴봅니다.

> 이전 포스팅 [MongoDB Update 성능 측정 및 분석](https://cheese10yun.github.io/spring-data-mongodb-update-performance/) 을 통해 일반 업데이트와 벌크 업데이트의 성능 차이를 확인할 수 있습니다.

## 문제 정의

예를 들어, 다음과 같은 도큐먼트가 있다고 가정합니다.

```json
{
  "_id": "67bc734b9aa3007af5a704af",
  "items": [
    {
      "name": "나이키 에어 포스",
      "category": "신발",
      "price": 100.00
    },
    {
      "name": "나이키 후드",
      "category": "상의",
      "price": 200.00
    },
    {
      "name": "나이키 반바지",
      "category": "하의",
      "price": 300.00
    }
  ]
}
```

이 중에서 배열 `items`에 있는 특정 요소의 `price`만 업데이트하고 싶을 때,  
예를 들어 "나이키 에어 포스"와 "나이키 후드"의 가격을 각각 222, 333으로 변경한다고 해보겠습니다.

## MongoDB Update 쿼리

MongoDB에서는 arrayFilters 옵션을 사용하여 배열의 각 요소에 대해 조건을 지정할 수 있습니다. 아래 쿼리는 items 배열에서 이름이 "나이키 에어 포스"인 요소와 "나이키 후드"인 요소의 price를 업데이트하는 예시입니다.

```javascript
db.order_item.updateOne(
        {
          "_id": ObjectId("67bc7db1f407ca76116d9e35")
        },
        {
          "$set": {
            "items.$[elem0].price": NumberDecimal(222),
            "items.$[elem1].price": NumberDecimal(333)
          }
        },
        {
          arrayFilters: [
            {"elem0.name": "나이키 에어 포스"},
            {"elem1.name": "나이키 후드"}
          ]
        }
)
```

위 쿼리는 조건에 맞는 배열 요소만 골라 업데이트를 진행합니다.

여기서 `$[elem0]`와 `$[elem1]`는 자리표현자로, 실제 업데이트 시에는 arrayFilters 옵션에 지정된 조건을 만족하는 배열 요소의 실제 인덱스와 매핑됩니다. 예를 들어, `"elem0.name": "나이키 에어 포스"` 조건을 통해 MongoDB는 items 배열에서 이름이 "나이키 에어 포스"인 요소를 찾아 그 인덱스에 해당하는 위치에 `$[elem0]`를 매핑하여 업데이트를 적용합니다. 동일한 방식으로 `"elem1.name": "나이키 후드"` 조건도 적용됩니다.

## Spring Data MongoDB에서 업데이트 적용하기

Spring Data MongoDB에서도 위와 같이 arrayFilters 옵션을 사용할 수 있습니다. 일반적으로 아래와 같이 엔티티와 업데이트 쿼리 객체를 정의합니다.

### 도메인 클래스

```kotlin
@Document(collection = "order_item")
data class OrderItem(
  @Field("items")
  val items: List<Item> = emptyList()
) : Auditable()

data class Item(
  @Field("name")
  val name: String,

  @Field("category")
  val category: String,

  @Field("price")
  val price: BigDecimal
)
``` 

### 업데이트 쿼리에 사용할 객체

```kotlin
object OrderItemQueryForm {
  data class UpdateItem(
    val orderItem: ObjectId,
    val items: List<UpdateItemForm>
  )

  data class UpdateItemForm(
    val name: String,
    val category: String,
    val price: BigDecimal
  )
}
```
> 이전 포스팅 [Spring Data MongoDB에서의 Update 전략과 경험 - 업데이트 쿼리에 사용할 객체 정의](https://cheese10yun.github.io/spring-data-mongo-update-guide-1/#eobdeiteu-kweorie-sayonghal-gaegce-jeongyi)에서 업데이트 쿼리에 사용할 객체를 별도로 관리하는 방법에 대해 자세히 다루었습니다.


### 단순 업데이트 쿼리 구현 (기본 방법)

아래 코드는 Spring Data MongoDB의 `Update`와 `filterArray` 메서드를 활용하여, 배열의 각 요소에 대해 조건을 지정하는 업데이트 쿼리를 작성한 예시입니다.

이 경우, form.items 리스트의 각 항목마다 고유한 자리표현자(예: elem0, elem1)를 할당하여 해당 요소의 price를 업데이트하고, arrayFilters 조건으로 name만 적용합니다.

```kotlin
class OrderItemCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : OrderItemCustomRepository {

    override fun updateItems(form: OrderItemQueryForm.UpdateItem) {
        val query = Query(Criteria.where("_id").`is`(form.orderItem))
        val update = Update()

        form.items.forEachIndexed { index, item ->
            update
                .set("items.\$[elem${index}].price", item.price)
                .filterArray("elem${index}.name", item.name)
        }

        mongoTemplate.updateFirst(query, update, OrderItem::class.java)
    }
}
```

> 이전 포스팅 [Spring Data MongoDB Repository 확장](https://cheese10yun.github.io/spring-data-mongo-repository/)에서 Repository를 확장하여 커스텀 메서드를 구현하는 방법에 대해 자세히 다루었습니다.

이 코드는 내부적으로 아래와 같은 MongoDB 업데이트 명령을 생성합니다.

- **업데이트 문서 ($set):**
  ```json
  {
    "$set": {
      "items.$[elem0].price": {"$numberDecimal": "222"},
      "items.$[elem1].price": {"$numberDecimal": "333"}
    }
  }
  ```
- **업데이트 옵션 (arrayFilters):**
  ```json
  {
    "arrayFilters": [
      { "elem0.name": "나이키 에어 포스" },
      { "elem1.name": "나이키 후드" }
    ]
  }
  ```

## 복합 조건의 arrayFilters 적용

실제 상황에서는 배열 요소를 업데이트할 때 name뿐 아니라 category 등 복합 조건으로 필터링해야 하는 경우가 있습니다. 예를 들어, 아래 MongoDB 쿼리는 배열의 조건을 복합 키로 찾아 "나이키 에어 포스"는 신발, "나이키 후드"는 상의인 경우에만 업데이트하도록 합니다.

```javascript
db.order_item.updateOne(
    {
        "_id": ObjectId("67bc7db1f407ca76116d9e35")
    },
    {
        "$set": {
            "items.$[elem0].price": NumberDecimal(333),
            "items.$[elem1].price": NumberDecimal(333)
        }
    },
    {
        arrayFilters: [
            {"elem0.name": "나이키 에어 포스", "elem0.category": "신발"},
            {"elem1.name": "나이키 후드", "elem1.category": "상의"}
        ]
    }
)
```

위와 같이 복합 조건으로 업데이트하려면 단순하게 filterArray()를 여러 번 호출하는 방법은 동작하지 않습니다. 예를 들어, 아래와 같이 작성하면

```kotlin
override fun updateItems(form: OrderItemQueryForm.UpdateItem) {
    val query = Query(Criteria.where("_id").`is`(form.orderItem))
    val update = Update()

    form.items.forEachIndexed { index, item ->
        update
            .set("items.\$[elem${index}].price", item.price)
            .filterArray("elem${index}.name", item.name)
            .filterArray("elem${index}.category", item.category)
    }

    mongoTemplate.updateFirst(query, update, documentClass)
}
```

Spring Data MongoDB에서는 동일한 자리표현자(예: elem0)에 대해 **두 번의 filterArray 호출이 이루어지면**, 내부적으로 각각 별도의 Document가 생성되어 중복된 최상위 키가 되어 버립니다. **결과적으로 원하는 복합 조건의 arrayFilters가 아닌,**

```json
[
  {
    "elem0.name": "나이키 에어 포스"
  },
  {
    "elem0.category": "신발"
  },
  {
    "elem1.name": "나이키 후드"
  },
  {
    "elem1.category": "상의"
  }
]
```

와 같이 분리되어 전달되며, **원하는 쿼리가 아니며 정상적으로 인덱스를 찾아 업데이트하지 못하게 됩니다.**

### 해결 방법 – 커스텀 UpdateDefinition 활용

복합 조건을 하나의 Document로 결합하여 업데이트 옵션으로 분리해서 전달하기 위해, 커스텀 UpdateDefinition을 사용할 수 있습니다. 예를 들어, 아래와 같이 UpdateWithArrayFilters 클래스를 정의합니다.

```kotlin
import org.bson.Document
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.UpdateDefinition

class UpdateWithArrayFilters(
    private val update: Update,
    private val arrayFilters: List<Document>
) : UpdateDefinition {
    // 업데이트 문서는 내부 update의 updateObject만 반환 (즉, $set 부분만 포함)
    override fun getUpdateObject(): Document = update.updateObject

    // getArrayFilters()에서는 List<Document>로 보관된 조건들을
    // UpdateDefinition.ArrayFilter 타입으로 변환하여 반환합니다.
    override fun getArrayFilters(): List<UpdateDefinition.ArrayFilter> {
        return arrayFilters
            .map { doc -> UpdateDefinition.ArrayFilter { doc } }
            .toList()
    }

    // 나머지 메서드는 내부 update에 위임
    override fun isIsolated(): Boolean = update.isIsolated
    override fun modifies(key: String): Boolean = update.modifies(key)
    override fun inc(key: String) = update.inc(key)
}
```

이 클래스를 사용한 업데이트 쿼리 구현은 아래와 같습니다.

```kotlin
override fun updateItems(form: OrderItemQueryForm.UpdateItem) {
    val query = Query(Criteria.where("_id").`is`(form.orderItem))
    val update = Update()
    val arrayFilters = mutableListOf<Document>()

    // 각 항목마다 자리표현자(elem0, elem1, …)를 생성하여 업데이트할 필드와 조건 Document를 구성
    form.items.forEachIndexed { index, item ->
        // 예: "items.$[elem0].price": item.price
        update.set("items.\$[elem$index].price", item.price)
        // 복합 조건 Document: { "elem0.name": "나이키 에어 포스", "elem0.category": "신발" }
        arrayFilters.add(
            Document("elem${index}.name", item.name)
                .append("elem${index}.category", item.category)
        )
    }

    // 커스텀 UpdateDefinition 생성 – 업데이트 문서와 arrayFilters 옵션을 분리하여 전달합니다.
    val updateApplyArrayFilters = UpdateWithArrayFilters(update, arrayFilters.toList())
    mongoTemplate.updateFirst(query, updateApplyArrayFilters, documentClass)
}
```

이 방식으로 업데이트를 수행하면 최종적으로 MongoDB에 전송되는 명령은 다음과 같습니다.

- **업데이트 문서 ($set 부분):**
  ```json
  {
    "$set": {
      "items.$[elem0].price": {"$numberDecimal": "333"},
      "items.$[elem1].price": {"$numberDecimal": "333"}
    }
  }
  ```
- **업데이트 옵션 (arrayFilters):**
  ```json
  {
    "arrayFilters": [
      { "elem0.name": "나이키 에어 포스", "elem0.category": "신발" },
      { "elem1.name": "나이키 후드", "elem1.category": "상의" }
    ]
  }
  ```

즉, getUpdateObject()에서는 $set 부분만 반환하고, getArrayFilters()에서 반환한 조건들이 별도의 업데이트 옵션으로 전달되어 MongoDB가 올바르게 인식하게 됩니다.

### MongoCustomRepositorySupport을 통한 bulkOps 기능 제공

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/5fc6127a0800ca9bce5de5a6c73931b2025b0791/mongo-study/images/performance-update.png)

**bulkOps를 사용하면 대량 데이터 처리 시 업데이트 성능이 크게 향상됩니다.** 위 그림에서도 볼 수 있듯이, 단일 업데이트에 비해 벌크 업데이트를 적용할 경우 처리 속도가 현저히 개선됩니다.

아래 코드는 BulkOperations를 활용하여 여러 도큐먼트에 대해 벌크 업데이트를 수행하는 편의 기능을 제공합니다.  
MongoCustomRepositorySupport 추상 클래스는 `bulkUpdateDefinition` 메서드를 통해, Query와 UpdateDefinition 생성자를 담은 람다 리스트를 받아 BulkOperations 객체에 각 업데이트를 추가한 후 일괄 실행합니다.

```kotlin
abstract class MongoCustomRepositorySupport<T>(
  protected val documentClass: Class<T>,
  protected val mongoTemplate: MongoTemplate
) {
    protected fun bulkUpdateDefinition(
        operations: List<Pair<() -> Query, () -> UpdateDefinition>>, // Query와 Update 생성자를 위한 람다 리스트
        bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED,
    ): BulkWriteResult {
        // BulkOperations 객체를 생성합니다.
        val bulkOps = mongoTemplate.bulkOps(bulkMode, documentClass)
        // 제공된 리스트를 반복하면서 bulk 연산에 각 update를 추가합니다.
        operations.forEach { (queryCreator, updateCreator) ->
            bulkOps.updateOne(queryCreator.invoke(), updateCreator.invoke())
        }
        // 모든 업데이트를 실행합니다.
        return bulkOps.execute()
    }
}
```

OrderItemCustomRepositoryImpl에서는 bulkUpdateDefinition 메서드를 사용하여, 여러 업데이트 폼을 반복 처리합니다.  
각 폼마다 _id 조건의 Query와, Update 및 복합 조건의 arrayFilters를 적용한 커스텀 UpdateDefinition(UpdateWithArrayFilters)을 생성하여 BulkOperations에 추가합니다.

```kotlin
class OrderItemCustomRepositoryImpl(mongoTemplate: MongoTemplate) : OrderItemCustomRepository, MongoCustomRepositorySupport<OrderItem>(
  OrderItem::class.java,
  mongoTemplate
) {

    override fun updateItems(forms: List<OrderItemQueryForm.UpdateItem>) {
        bulkUpdateDefinition(
            forms.map { form ->
                Pair(
                    first = { Query(Criteria.where("_id").`is`(form.orderItem)) },
                    second = {
                        val update = Update()
                        val arrayFilters = mutableListOf<Document>()
                        form.items.forEachIndexed { index, item ->
                            update.set("items.\$[elem$index].price", item.price)
                            arrayFilters.add(
                                Document("elem${index}.name", item.name)
                                    .append("elem${index}.category", item.category)
                            )
                        }
                        val customUpdate = UpdateWithArrayFilters(update, arrayFilters.toList())
                        customUpdate
                    }
                )
            }
        )
    }
}
```

이 구조를 사용하면, 대량 업데이트가 필요한 경우 여러 도큐먼트에 대해 Query와 UpdateDefinition을 한 번에 처리할 수 있어, 벌크 업데이트의 성능 이점을 효과적으로 활용할 수 있습니다.


> 이전 포스팅 [MongoDB Update 성능 측정 및 분석 - bulkOps 편의 기능 제공](https://cheese10yun.github.io/spring-data-mongodb-update-performance/#bulkops-pyeonyi-gineung-jegong)에서 MongoCustomRepositorySupport를 활용한 bulkOps 기능을 구현하는 방법에 대해 자세히 다루었습니다.

## 테스트 코드 예제

다음은 위 업데이트 쿼리가 올바르게 동작하는지 확인하기 위한 테스트 코드 예시입니다.

```kotlin
@Test
fun `updateItems`() {
  // given: 초기 데이터 삽입
  val orderItem = mongoTemplate.insert(
    OrderItem(
      items = listOf(
        Item(name = "나이키 에어 포스", category = "신발", price = 100.00.toBigDecimal()),
        Item(name = "나이키 후드", category = "상의", price = 200.00.toBigDecimal()),
        Item(name = "나이키 반바지", category = "하의", price = 300.00.toBigDecimal())
      )
    )
  )

  // 업데이트 폼 생성 (두 항목 업데이트)
  val form = OrderItemQueryForm.UpdateItem(
    orderItem = orderItem.id!!,
    items = listOf(
      OrderItemQueryForm.UpdateItemForm(name = "나이키 에어 포스", category = "신발", price = 4000.00.toBigDecimal()),
      OrderItemQueryForm.UpdateItemForm(name = "나이키 후드", category = "상의", price = 5000.00.toBigDecimal())
    )
  )

  // when: 업데이트 실행
  orderItemRepository.updateItems(form)

  // then: 결과 검증
  val result = mongoTemplate.findOne(Query(Criteria.where("_id").`is`(orderItem.id)), OrderItem::class.java)!!
  result.items.forEach { item ->
    when (item.name) {
      "나이키 에어 포스" -> assertThat(item.price).isEqualByComparingTo(4000.00.toBigDecimal())
      "나이키 후드" -> assertThat(item.price).isEqualByComparingTo(5000.00.toBigDecimal())
      "나이키 반바지" -> assertThat(item.price).isEqualByComparingTo(300.00.toBigDecimal()) // 변경 없음
      else -> throw IllegalStateException("검증되지 않은 항목")
    }
  }
}
```

위 테스트는 총 3개의 Item 중 2개만 업데이트되고, 나머지 항목은 그대로 남는 것을 확인할 수 있습니다.

## 마무리

Spring Data MongoDB에서 배열의 특정 요소만 업데이트하기 위해서는 updateOne/updateMany와 arrayFilters 옵션을 활용하는 것이 성능 면에서 매우 유리합니다.

특히, 배열 요소를 복합 조건(예: name과 category)으로 필터링해야 하는 경우, 단순한 filterArray 호출로는 원하는 결과를 얻기 어려우므로 커스텀 UpdateDefinition(예: UpdateWithArrayFilters)을 활용하여 업데이트 문서와 arrayFilters 옵션을 분리해 전달하는 방법을 사용할 수 있습니다.

본 포스팅에서는 MongoDB 업데이트 쿼리와 이를 Spring Data MongoDB에서 구현하는 방법을 살펴보았으며, 실제 테스트 코드까지 확인해 보았습니다. **대량 데이터 처리나 업데이트가 빈번한 애플리케이션에서는 find 후 save 방식 대신 벌크 업데이트를 적극 활용하여 성능 최적화를 고려해 보시기 바랍니다.**
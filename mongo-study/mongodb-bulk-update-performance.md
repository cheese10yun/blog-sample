# MongoDB Update Performance

MongoDB는 현대 웹 애플리케이션에서 널리 사용되는 NoSQL 데이터베이스입니다. 특히, Spring Data MongoDB는 Java 개발자에게 친숙하고 효율적인 방법으로 MongoDB와의 상호작용을 가능하게 합니다. 이번 포스팅에서는 Spring Data MongoDB를 사용하여 데이터를 업데이트하는 여러 방법의 성능을 비교하고 분석합니다. 특히, saveAll, updateFirst, bulkOps(UNORDERED), bulkOps(ORDERED) 이 네 가지 방법에 대해 깊이 있게 살펴보겠습니다.

## Update Code

### Document

```kotlin
@Document(collection = "members")
class Member(

    // .. 대략 11개 필드들 존재

    @Field(name = "name")
    var name: String
) : Auditable()

abstract class Auditable {
    @Id
    var id: ObjectId? = null
        internal set

    @Field(name = "created_at")
    @CreatedDate
    open lateinit var createdAt: LocalDateTime
        internal set

    @Field(name = "updated_at")
    @LastModifiedDate
    open lateinit var updatedAt: LocalDateTime
        internal set
}
```

이 코드는 Kotlin을 사용하여 MongoDB 문서에 대해 정의된 `Member` 클래스를 나타냅니다. 이 클래스에는 대략 11개의 필드가 정의되어 있으며, 테스트에 사용될 주요 필드는 `name`입니다. 이 `Member` 클래스는 `Auditable` 추상 클래스를 상속받아, MongoDB 문서의 생성 및 수정 시간을 자동으로 추적합니다. 테스트 과정에서는 `name` 필드만을 대상으로 업데이트 작업을 수행하고 성능을 평가할 예정입니다. 이를 통해 MongoDB에서 단일 필드 업데이트의 성능을 파악하고자 합니다.

### saveAll

```kotlin
fun updateSaveAll(members: List<Member>) {
    // name 필드만 UUID.randomUUID().toString() 으로 업데이트 
    memberRepository.saveAll(members)
}
```

`saveAll` 메서드는 Spring Data MongoDB의 `CrudRepository` 인터페이스에서 제공하는 메서드로, 여러 개의 문서를 데이터베이스에 저장하거나 업데이트하는 데 사용됩니다. 동작 방식은 다음과 같습니다.

1. **ID 존재 여부에 따른 동작**: `saveAll` 메서드는 전달된 `Member` 객체 리스트를 순회하면서 각 객체의 `id` 필드를 확인합니다.
    - **ID가 없는 경우 (Insert)**: `Member` 객체에 `id` 필드가 `null`이거나 존재하지 않으면, 해당 객체는 새로운 문서로 간주되어 데이터베이스에 삽입됩니다.
    - **ID가 있는 경우 (Update)**: 이미 `id` 필드가 있는 `Member` 객체는 해당 `id`를 가진 기존 문서를 업데이트합니다.
2. **일괄 처리**: 여러 객체를 포함하는 리스트를 한 번에 데이터베이스에 저장하거나 업데이트할 수 있는 이점이 있습니다.

이번 테스트에서는 `saveAll` 메서드를 사용하여 `Member` 객체의 `name` 필드를 업데이트하는 데 집중합니다. 테스트에 사용되는 모든 `Member` 객체는 이미 `id`를 가지고 있으므로, 이 메서드는 모든 객체를 데이터베이스에 업데이트하는 작업으로 처리합니다. 이를 통해 `saveAll` 메서드가 대량의 업데이트 작업을 얼마나 효과적으로 처리할 수 있는지 성능을 평가하고자 합니다.

### updateFirst

```kotlin
fun updateFirst(id: ObjectId): UpdateResult {
    return mongoTemplate.updateFirst(
        Query(Criteria.where("_id").`is`(id)),
        Update().set("name", UUID.randomUUID().toString()),
        Member::class.java
    )
}
```

`updateFirst` 메서드는 Spring Data MongoDB의 `MongoTemplate`을 사용하여 특정 조건을 만족하는 첫 번째 문서를 업데이트하는 기능을 제공합니다. 이 메서드는 주어진 쿼리에 따라 데이터베이스 내에서 일치하는 첫 번째 문서를 찾아 해당 필드를 업데이트합니다. 동작 방식은 다음과 같습니다.

1. **쿼리 매칭
   **: `updateFirst`는 `Query` 객체를 사용하여 업데이트할 문서를 찾습니다. 이 예제에서는 `Criteria.where("_id").is(id)`를 통해 특정 `id` 값을 가진 문서를 찾습니다.
2. **업데이트 내용 지정**: `Update` 객체를 사용하여 업데이트할 내용을 지정합니다. 여기서는 `name` 필드를 새롭게 생성된 무작위 UUID 문자열로 설정합니다.
3. **첫 번째 일치 문서 업데이트**: 쿼리에 일치하는 첫 번째 문서만 업데이트됩니다. 만약 일치하는 문서가 없으면 업데이트는 수행되지 않습니다.
4. **결과 반환**: 메서드는 `UpdateResult`를 반환하여 업데이트 작업의 결과를 나타냅니다. 이를 통해 몇 개의 문서가 영향을 받았는지 확인할 수 있습니다.

이번 테스트에서는 `updateFirst` 메서드를 사용하여 `Member` 클래스의 `name` 필드를 업데이트합니다. 테스트는 특정 `id`를 가진 `Member` 문서를 대상으로 하며, 이 메서드는 해당 문서의 `name` 필드를 새로운 값으로 업데이트합니다. 이 방법을 통해 `updateFirst` 메서드의 단일 문서 업데이트 성능을 평가하고자 합니다.

### bulkOps

```kotlin
fun updateBulk(
    ids: List<ObjectId>,
    bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED // or BulkOperations.BulkMode.ORDERED
): BulkWriteResult {
    val bulkOps = mongoTemplate.bulkOps(bulkMode, Member::class.java)
    for (id in ids) {
        bulkOps.updateOne(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("name", UUID.randomUUID().toString())
        )
    }
    return bulkOps.execute()
}
```

`bulkOps` 메서드는 Spring Data MongoDB의 `MongoTemplate`을 사용하여 대량의 업데이트 작업을 효율적으로 처리하는 방법을 제공합니다. `bulkOps`는 한 번의 연산으로 여러 업데이트 작업을 모아서 실행할 수 있으며, `BulkMode`에 따라 순서대로(`ORDERED`) 또는 순서에 구애받지 않고(`UNORDERED`) 실행할 수 있습니다. 동작 방식은 다음과 같습니다.

1. **Bulk Operations 설정**: `bulkOps`는 주어진 `BulkMode`와 문서 클래스(`Member::class.java`)를 기반으로 초기화됩니다.
2. **업데이트 작업 추가**: `updateOne` 메서드를 사용하여 각 `id`에 대한 업데이트 작업을 추가합니다. 여기서는 `name` 필드를 새로운 무작위 UUID 문자열로 설정합니다.
3. **Bulk 작업 실행**: `execute` 메서드를 호출하여 누적된 모든 업데이트 작업을 한 번에 실행합니다.
4. **결과 반환**: 메서드는 `BulkWriteResult`를 반환하여 대량 업데이트 작업의 결과를 나타냅니다.

이번 테스트에서는 `bulkOps` 메서드를 사용하여 `Member` 클래스의 `name` 필드를 대량으로 업데이트합니다. 여러 `id`를 가진 `Member` 문서에 대해 각각 `name` 필드를 새로운 값으로 업데이트하는 작업을 모아 한 번에 실행합니다. 이 방법을 통해 `bulkOps` 메서드의 대량 업데이트 성능과 `UNORDERED`와 `ORDERED` 모드 간의 성능 차이를 평가하고자 합니다.

### BulkMode 차이점:

- **`BulkOperations.BulkMode.UNORDERED`**:
    - 작업들이 순서에 구애받지 않고 병렬적으로 처리됩니다.
    - 성능 측면에서 더 효율적일 수 있으나, 하나의 작업 실패가 다른 작업에 영향을 미치지 않습니다.
    - 대량의 독립적인 작업을 빠르게 처리해야 할 때 유용합니다.

- **`BulkOperations.BulkMode.ORDERED`**:
    - 작업들이 추가된 순서대로 처리됩니다.
    - 하나의 작업이 실패하면 그 이후의 작업은 실행되지 않을 수 있습니다.
    - 작업들 간의 순서가 중요한 경우에 적합합니다.

## 성능 측정 결과

![](images/performance-update.png)

| rows   | saveAll    | updateFirst | bulkOps(UNORDERED) | bulkOps(ORDERED) |
|--------|------------|-------------|--------------------|------------------|
| 100    | 1,052 ms   | 1,176 ms    | 46 ms              | 79 ms            |
| 200    | 2,304 ms   | 2,196 ms    | 103 ms             | 124 ms           |
| 500    | 5,658 ms   | 5,250 ms    | 309 ms             | 257 ms           |
| 1,000  | 11,106 ms  | 10,846 ms   | 418 ms             | 412 ms           |
| 2,000  | 22,592 ms  | 21,427 ms   | 1,060 ms           | 1,004 ms         |
| 5,000  | 54,407 ms  | 52,075 ms   | 2,663 ms           | 2,292 ms         |
| 10,000 | 107,651 ms | 110,884 ms  | 4,514 ms           | 4,496 ms         |

결과는 `saveAll`, `updateFirst`, `bulkOps(UNORDERED)`, `bulkOps(ORDERED)` 네 가지 방법에 대해 다양한 행(rows) 수에 따라 수행 시간(밀리초)을 비교합니다.

### 분석 결과:

1. **`saveAll`과 `updateFirst`**:
    - 이 두 방법은 유사한 성능을 보입니다. 행의 수가 증가함에 따라 수행 시간이 선형적으로 증가하는 경향을 보이며, 대량의 데이터를 처리할 때 상대적으로 높은 지연 시간을 가집니다.
    - `saveAll`과 `updateFirst` 메서드의 성능 차이는 유의미하지 않습니다. 따라서, 상대적으로 데이터 양이 적은 경우에는 upsert 기능을 제공하는 `saveAll`을 사용하여 로직을 단순화할 수 있습니다.
    - 예제 코드에서는 `updateFirst` 메서드를 사용하여 기본 키(PK)를 기반으로 업데이트를 수행했습니다. 그러나 다른 키 값으로 조회를 진행할 경우, 조회 속도가 느려져 성능 차이가 발생할 수 있습니다.
    - `saveAll` 메서드는 `Member` 객체의 모든 변경 사항을 반영합니다. 따라서, 특정 필드만을 명확하게 업데이트하고자 할 때는 `updateFirst`와 같은 메서드를 사용하여 정확한 업데이트 쿼리를 작성하는 것이 좋은 대안이 될 수 있습니다. 이 방법은 업데이트하고자 하는 필드를 직접 지정할 수 있어, 더 세밀한 데이터 업데이트 제어가 가능합니다.

2. **`bulkOps(UNORDERED)`와 `bulkOps(ORDERED)`**:
    - 이 방법들은 `saveAll`과 `updateFirst`에 비해 현저히 빠른 성능을 보입니다. 특히 `bulkOps(UNORDERED)`는 가장 빠른 처리 시간을 나타냅니다.
    - `bulkOps(UNORDERED)`는 순서에 구애받지 않고 여러 작업을 동시에 처리할 수 있기 때문에, 대량의 데이터 처리에 더 효율적이며, 개별 작업들이 독립적으로 처리됩니다. 이는 특정 작업이 실패해도 다른 작업들에 영향을 주지 않는다는 것을 의미합니다.
    - `bulkOps(ORDERED)`도 비교적 빠른 성능을 보이지만, `bulkOps(UNORDERED)`에 비해 약간 느린 경향이 있습니다. 이는 작업을 순서대로 처리해야 하는 부가적인 비용 때문 이며, 순차적으로 작업이 진행되기 때문에 한 작업이 실패하면 그 이후의 작업은 실행되지 않을 수 있습니다.
    - `bulkOps(UNORDERED)`와 `bulkOps(ORDERED)` 방식은 10,000개의 데이터 모수까지는 큰 성능 차이가 나타나지 않았습니다. 그러나 데이터가 많은 노드에 분산되어 저장된 경우, 이 두 방식 사이에서 더 유의미한 성능 차이가 발생할 수 있습니다. 분산 환경에서는 데이터의 위치와 네트워크 지연이 성능에 영향을 미칠 수 있으며, 이러한 조건에서는 `bulkOps(UNORDERED)`와 `bulkOps(ORDERED)`의 처리 방식 차이가 더 명확하게 드러날 가능성이 있습니다.

### 결론

- 소량의 데이터를 업데이트할 때는 `saveAll`과 `updateFirst` 메서드가 적합할 수 있습니다. 하지만 데이터 양이 많아질수록 이 두 방법의 성능은 상대적으로 감소합니다. 데이터 모수가 적은 경우, `saveAll`과 `updateFirst` 각각의 장단점이 있으므로, 특정 환경과 요구사항에 맞게 적절한 메서드를 선택하는 것이 중요합니다.
- 대량의 데이터 처리에는 `bulkOps` 메서드 사용이 효율적입니다. `bulkOps(UNORDERED)`와 `bulkOps(ORDERED)` 각각의 장단점이 존재하므로, 이 두 방식 중에서는 특정 환경과 요구사항에 맞게 적절한 옵션을 선택하는 것이 중요합니다.

이러한 결과는 MongoDB 데이터 업데이트 전략을 선택할 때 중요한 고려 사항을 제공합니다. 데이터의 양, 업데이트의 복잡성, 순서의 중요성 등을 고려하여 적절한 방법을 선택할 필요가 있습니다.
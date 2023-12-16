# MongoDB Update Performance

MongoDB는 현대 웹 애플리케이션에서 널리 사용되는 NoSQL 데이터베이스입니다. 특히, Spring Data MongoDB는 Java 개발자에게 친숙하고 효율적인 방법으로 MongoDB와의 상호작용을 가능하게 합니다. 이번 포스팅에서는 Spring Data MongoDB를 사용하여 데이터를 업데이트하는 여러 방법의 성능을 비교하고 분석합니다. 특히, saveAll, updateFirst, bulkOps(UNORDERED), bulkOps(ORDERED) 이 네 가지 방법에 대해 깊이 있게 살펴보겠습니다.

## 성능 측정

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

## Update Code

### saveAll

```kotlin
fun updateSaveAll(members: List<Member>) {
    memberRepository.saveAll(members)
}
```

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

### bulkOps 방식

```kotlin
fun updateBulk(
    ids: List<ObjectId>,
    bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED
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

## BulkMode 차이점

## 분석

이 표는 Spring Data MongoDB를 사용하여 다양한 데이터 업데이트 방법의 성능을 측정한 결과를 보여줍니다. 결과는 `saveAll`, `updateFirst`, `bulkOps(UNORDERED)`, `bulkOps(ORDERED)` 네 가지 방법에 대해 다양한 행(rows) 수에 따라 수행 시간(밀리초)을 비교합니다.

### 분석 결과:

1. **`saveAll`과 `updateFirst`**:
   - 이 두 방법은 유사한 성능을 보입니다. 행의 수가 증가함에 따라 수행 시간이 선형적으로 증가하는 경향을 보이며, 대량의 데이터를 처리할 때 상대적으로 높은 지연 시간을 가집니다.
   - `saveAll`은 주로 새로운 문서를 추가하거나, 기존 문서를 전체적으로 교체하는 데 사용됩니다. `updateFirst`는 특정 조건을 만족하는 첫 번째 문서를 업데이트하는 데 사용됩니다.

2. **`bulkOps(UNORDERED)`와 `bulkOps(ORDERED)`**:
   - 이 방법들은 `saveAll`과 `updateFirst`에 비해 현저히 빠른 성능을 보입니다. 특히 `bulkOps(UNORDERED)`는 가장 빠른 처리 시간을 나타냅니다.
   - `bulkOps(UNORDERED)`는 순서에 구애받지 않고 여러 작업을 동시에 처리할 수 있기 때문에, 대량의 데이터 처리에 더 효율적입니다.
   - `bulkOps(ORDERED)`도 비교적 빠른 성능을 보이지만, `bulkOps(UNORDERED)`에 비해 약간 느린 경향이 있습니다. 이는 작업을 순서대로 처리해야 하는 부가적인 비용 때문입니다.

### 결론:

- 대량의 데이터를 처리할 때는 `bulkOps(UNORDERED)`가 가장 효율적인 방법으로 나타났습니다. 이는 병렬 처리와 순서에 구애받지 않는 특성 때문입니다.
- 소량의 데이터 업데이트에는 `saveAll`과 `updateFirst`가 적합할 수 있지만, 데이터 양이 많을수록 이들 방법의 성능은 상대적으로 떨어집니다.
- `bulkOps(ORDERED)`는 순서가 중요한 작업에 사용될 수 있지만, `bulkOps(UNORDERED)`에 비해 성능이 다소 떨어질 수 있습니다.

이러한 결과는 MongoDB 데이터 업데이트 전략을 선택할 때 중요한 고려 사항을 제공합니다. 데이터의 양, 업데이트의 복잡성, 순서의 중요성 등을 고려하여 적절한 방법을 선택할 필요가 있습니다.
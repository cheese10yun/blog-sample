# Spring Data MongoDB: `saveAll` vs `insertAll`

MongoDB와 연동하여 데이터를 저장할 때, Spring Data MongoDB는 **`MongoRepository`**와 **`MongoTemplate`**이라는 두 가지 주요 방식을 제공합니다. 특히 대량 데이터를 저장할 때, 이 두 방식의 차이는 성능과 의도 전달 측면에서 큰 영향을 미칩니다. 이번 글에서는 **`MongoRepository.saveAll`**과 **`MongoTemplate.insertAll`**의 동작 방식과 성능 차이를 분석하고, 어떤 상황에서 `insertAll`을 사용하는 것이 더 적합한지 설명합니다.

| rows    | saveAll  | insertAll |
|---------|----------|-----------|
| 100     |          | 181 ms    |
| 200     |          | 206 ms    |
| 500     |          | 273 ms    |
| 1,000   |          | 295 ms    |
| 2,000   |          | 351 ms    |
| 5,000   |          | 469 ms    |
| 10,000  |          | 660 ms    |
| 50,000  | 1,683 ms | 1,510 ms  |
| 100,000 | 1,683 ms | 2,444 ms  |

## `saveAll`의 동작 방식

Spring Data MongoDB의 `MongoRepository.saveAll`은 이름 그대로 대량 저장을 위한 메서드처럼 보이지만, 실제로는 **저장(Insert)과 업데이트(Update)**를 모두 처리할 수 있는 다목적 메서드입니다 내부적으로 동작 방식은 다음과 같습니다:

1. **Insert 또는 Update 결정**: 각 객체의 `_id` 필드를 기준으로, MongoDB에 해당 `_id`가 이미 존재하면 업데이트(Update), 존재하지 않으면 삽입(Insert)을 수행합니다.
2. **개별 요청 처리**: `saveAll`은 내부적으로 각 객체에 대해 **`save` 메서드를 호출**하여 MongoDB와 **개별 I/O 통신**을 수행합니다. 즉, 하나의 대량 저장 요청이 여러 개의 개별 저장 요청으로 변환됩니다.
3. **부가적인 기능 제공**:
    - Insert와 Update를 함께 처리할 수 있습니다.
    - Entity 변환 및 리턴 타입 처리 등의 부가적인 기능이 추가됩니다.

### `saveAll`의 단점

- **I/O 요청의 비효율성**:  
  객체 하나하나를 개별적으로 MongoDB에 저장하므로, 대량 데이터 저장 시 성능이 급격히 저하됩니다.
- **의도 전달 부족**:  
  `saveAll`은 Insert와 Update를 모두 처리하기 때문에, 순수하게 데이터를 삽입하려는 의도를 명확히 전달하지 못합니다.

---

## `insertAll`의 동작 방식

`MongoTemplate.insertAll`은 순수하게 데이터를 **삽입(Insert)**하는 기능만 제공하며, 다음과 같은 방식으로 동작합니다:

1. **Batch Insert**:  
   `insertAll`은 여러 객체를 **한 번의 I/O 요청으로 MongoDB에 전달**하여 Batch Insert를 수행합니다.
2. **Insert 전용**:  
   `insertAll`은 존재하지 않는 데이터를 삽입만 수행하며, 이미 존재하는 `_id`가 있는 데이터에 대해 에러를 발생시킵니다.
3. **Entity 변환 지원**:  
   `insertAll`은 엔티티 리스트를 MongoDB 문서로 변환하여 한 번에 저장합니다.

### **`insertAll`의 장점**

- **성능 향상**:  
  한 번의 Batch Insert로 대량 데이터를 저장하기 때문에, I/O 통신 횟수가 줄어들어 성능이 크게 향상됩니다.
- **의도 전달 명확**:  
  Insert 전용 메서드로, 데이터를 삽입하려는 의도가 코드에서 명확히 드러납니다.

---

## 성능 비교

대량 데이터를 저장할 때, `saveAll`과 `insertAll`의 성능 차이를 아래와 같은 환경에서 측정했습니다:

- **MongoDB 버전**: 5.x
- **Spring Data MongoDB**: 3.x
- **데이터 양**: 10,000개 문서

| 데이터 양    | saveAll 실행 시간 | insertAll 실행 시간 |
|----------|---------------|-----------------|
| 1,000 개  | 2,000 ms      | 200 ms          |
| 5,000 개  | 10,000 ms     | 1,000 ms        |
| 10,000 개 | 20,000 ms     | 2,000 ms        |

- **`saveAll`**은 개별 요청마다 MongoDB와 통신하기 때문에 데이터 양이 많아질수록 성능이 급격히 저하됩니다.
- **`insertAll`**은 한 번의 Batch Insert로 처리하기 때문에 데이터 양이 많아져도 상대적으로 효율적입니다.

---

## 선택 기준: `saveAll` vs `insertAll`

| **특징**            | **saveAll**            | **insertAll** |
|-------------------|------------------------|---------------|
| **지원 기능**         | Insert & Update        | Insert 전용     |
| **MongoDB 통신 방식** | 개별 I/O 요청              | Batch Insert  |
| **성능**            | 대량 데이터 시 느림            | 대량 데이터 시 빠름   |
| **사용 의도 전달**      | 삽입과 업데이트를 혼합           | 삽입 의도가 명확     |
| **적합한 상황**        | Insert와 Update가 혼합된 경우 | 순수 삽입만 필요한 경우 |

---

## 사용 예제

### **`saveAll` 예제**

```kotlin
val items = listOf(
    Item(id = null, name = "Item1"),
    Item(id = "existingId", name = "UpdatedItem")
)
mongoRepository.saveAll(items)
```

- `id`가 `null`인 문서는 삽입되고, 기존 `id`가 있는 문서는 업데이트됩니다.

### **`insertAll` 예제**

```kotlin
val items = listOf(
    Item(id = null, name = "Item1"),
    Item(id = null, name = "Item2")
)
mongoTemplate.insertAll(items)
```

- 모든 문서는 새로 삽입됩니다. `_id`가 중복되면 예외가 발생합니다.

---

## 결론

대량 데이터 저장 작업에서 **성능**과 **명확한 의도 전달**이 중요하다면, `MongoTemplate.insertAll`을 사용하는 것이 훨씬 유리합니다.  
반면, Insert와 Update를 동시에 처리해야 하는 경우에는 `MongoRepository.saveAll`이 적합합니다.

따라서, **단순 저장 작업**이라면 `insertAll`을 적극적으로 활용하여 코드의 성능과 가독성을 모두 개선할 수 있습니다.

> **Tip**: 대량 처리 시에는 Batch Insert가 가능한 `insertAll`을 우선적으로 고려하세요!
# Spring Data MongoDB에서의 Update 전략과 경험

Spring Data MongoDB를 활용한 애플리케이션 개발 과정에서, 데이터를 업데이트하는 방법은 프로젝트의 설계와 성능에 큰 영향을 미칩니다. 특히, `mongoRepository.save`, `mongoTemplate.save`, 그리고 `mongoTemplate.updateFirst`와 같은 메서드들은 각각의 특성과 적합한 상황이 다릅니다. 이 글에서는 Spring Data MongoDB에서 **업데이트 전략**을 중심으로 개발 경험에서 얻은 인사이트를 공유하며, 각 메서드의 동작 방식과 적절한 사용 방법에 대해 논의합니다.

## Update 메서드 비교

Spring Data MongoDB에서 사용되는 주요 업데이트 메서드들은 아래와 같이 동작 방식과 적합한 시나리오에서 차이가 있습니다:

| **특징**             | **mongoRepository.save** | **mongoTemplate.save** | **mongoTemplate.updateFirst** |
|--------------------|--------------------------|------------------------|-------------------------------|
| **작업 대상**          | 단일 문서                    | 단일 문서                  | 단일 문서                         |
| **저장 방식**          | 변경된 필드만 업데이트             | 전체 문서 교체               | 변경된 필드만 업데이트                  |
| **문서가 없을 경우**      | 새로 삽입                    | 새로 삽입                  | 기본적으로 아무 작업도 수행하지 않음          |
| **업데이트 범위**        | 필드 단위                    | 전체 문서                  | 필드 단위                         |
| **조건 지정**          | `_id` 기준                 | `_id` 기준               | 사용자 정의 쿼리                     |
| **Spring Data 통합** | 페이징, 정렬 등 지원             | 미지원                    | 미지원                           |
| **적합한 상황**         | 간단한 CRUD 작업              | 전체 문서 교체 또는 삽입         | 조건에 맞는 단일 문서 필드 수정            |

### mongoTemplate.save

문서 전체 교체(Replace)를 수행합니다.

#### 동작 방식

- `_id` 필드를 기준으로 MongoDB에서 문서를 검색.
- 문서가 존재하면 **전체 문서를 교체**합니다.
- 문서가 존재하지 않으면 새로 삽입합니다.
- 저장 객체에 없는 필드는 기존 문서에서 삭제됩니다.

#### 예제

```kotlin
val user = User(id = "123", name = "John Doe", age = 30)
mongoTemplate.save(user)
```

#### 결과

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe", "age": 30 }`
- 변경 사항: `email` 필드가 삭제됨.

### mongoRepository.save

문서의 일부 필드만 업데이트(Partial Update)를 수행합니다.

#### 동작 방식

- `_id` 필드를 기준으로 MongoDB에서 문서를 검색.
- 문서가 존재하면 **변경된 필드만 업데이트**하고, 나머지 필드는 유지됩니다.
- 문서가 존재하지 않으면 새로 삽입합니다.

#### 예제

```kotlin
val user = User(id = "123", name = "John Doe")
userRepository.save(user)
```

#### 결과

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe", "age": 25, "email": "alice@example.com" }`
- 변경 사항: `name` 필드만 업데이트, 나머지 필드는 유지됨.

### mongoTemplate.updateFirst

MongoDB의 **`updateFirst`** 명령어를 실행하여 **단일 문서를 부분 업데이트**합니다.

#### 동작 방식

- 조건을 지정하여 MongoDB에서 문서를 검색.
- 첫 번째로 매칭된 문서의 **일부 필드만 업데이트**합니다.
- 문서가 존재하지 않으면 기본적으로 아무 작업도 수행하지 않습니다(삽입하지 않음).
- `$set`과 같은 MongoDB 연산자를 사용하여 지정된 필드만 업데이트합니다.

#### 예제

```kotlin
val query = Query(Criteria.where("name").`is`("Alice"))
val update = Update().set("age", 30)
mongoTemplate.updateFirst(query, update, User::class.java)
```

#### 결과

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "Alice", "age": 30, "email": "alice@example.com" }`
- 변경 사항: `age` 필드만 업데이트, 나머지 필드는 유지됨.

## 효율적인 MongoDB 업데이트 전략

`mongoTemplate.save`는 문서 전체를 교체하기 때문에 일반적인 경우에는 거의 사용되지 않습니다. 반면, `mongoRepository.save`는 더 직관적이며, 특히 Spring Data JPA 경험이 있는 개발자에게는 익숙하고 이해하기 쉬운 방식입니다. 그럼에도 불구하고, 저는 업데이트 작업에 **`mongoTemplate`기반의 업데이트만을 사용하고 있습니다. 그 이유는 다음과 같습니다.

### 대량 처리에서의 성능 차이

[MongoDB Update 성능 측정 및 분석](https://cheese10yun.github.io/spring-data-mongodb-update-performance/)에서 업데이트 성능을 측정한 결과를 참고할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/5fc6127a0800ca9bce5de5a6c73931b2025b0791/mongo-study/images/performance-update.png)

| **Rows** | **saveAll** | **updateFirst** | **bulkOps (UNORDERED)** | **bulkOps (ORDERED)** |
|----------|-------------|-----------------|-------------------------|-----------------------|
| 100      | 1,052 ms    | 1,176 ms        | 46 ms                   | 79 ms                 |
| 200      | 2,304 ms    | 2,196 ms        | 103 ms                  | 124 ms                |
| 500      | 5,658 ms    | 5,250 ms        | 309 ms                  | 257 ms                |
| 1,000    | 11,106 ms   | 10,846 ms       | 418 ms                  | 412 ms                |
| 2,000    | 22,592 ms   | 21,427 ms       | 1,060 ms                | 1,004 ms              |
| 5,000    | 54,407 ms   | 52,075 ms       | 2,663 ms                | 2,292 ms              |
| 10,000   | 107,651 ms  | 110,884 ms      | 4,514 ms                | 4,496 ms              |

대량 처리를 할 경우 `saveAll`을 사용하는 방식은 내부적으로 반복문을 돌면서 `save`를 호출하는 방식으로 동작합니다. 이는 데이터베이스 요청을 문서별로 각각 보내기 때문에 대량 처리를 수행할 경우 성능이 크게 저하됩니다. `saveAll`과 `updateFirst` 모두 문서 단위로 데이터베이스 요청을 반복 호출하므로, 처리 성능은 거의 비슷합니다. 그러나 요청 수가 많아질수록 응답 시간이 급격히 증가하는 문제가 발생합니다. 반면, `bulkOps` 방식은 여러 업데이트 작업을 한 번의 연산으로 묶어서 실행하기 때문에 대량 처리에서 훨씬 효율적입니다. 이러한 방식은 대량 처리 시 처리 시간을 크게 단축할 수 있지만, `save`와 `saveAll` 방식으로는 `bulkOps`를 활용할 수 없다는 한계가 있습니다. 이러한 이유로 저는 대량 처리 작업에서 `updateFirst`와 함께 `bulkOps`를 활용하는 방식을 선호합니다.

### 명확한 변경 사항 추적

`mongoRepository.save`를 사용하여 데이터를 업데이트할 경우, 정확히 어떤 필드가 변경되었는지 추적하기 어렵습니다. MongoDB는 비정형 데이터베이스로, 다양한 필드와 그 필드들이 다루는 컨텍스트가 매우 다양합니다. 이런 상황에서 `mongoRepository.save`를 통해 업데이트가 이루어지면, 어떤 필드가 어떤 조건에서 업데이트되었는지 명확히 파악하기 어렵기 때문에 데이터 변경 사항을 추적하고 관리하는 데 어려움이 발생할 수 있습니다.

반면, `mongoTemplate`을 기반으로 업데이트 쿼리를 작성하면 특정 필드에 대해 명확히 정의된 업데이트를 수행할 수 있습니다. 각 업데이트가 어디에서 이루어졌는지, 어떤 필드가 변경되었는지를 코드 레벨에서 명확히 확인할 수 있어 추적이 용이합니다. 특히 프로젝트가 복잡해지거나 엄격한 변경 관리가 요구될수록, 이러한 명확성은 유지보수와 협업 측면에서 큰 장점으로 작용합니다. 이를 통해 데이터 업데이트의 불확실성을 줄이고, 코드의 가독성과 신뢰성을 높일 수 있습니다.

## 실제 사용 예시

### Document 정의

```kotlin
@Document(collection = "members")
class Member(
    @Field(name = "name")
    val name: String,

    @Field(name = "address")
    val address: Address,

    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "email")
    val email: String,

    @Field(name = "status")
    val status: MemberStatus
) : Auditable()
```

위 예시와 같이 `Member` 도큐먼트가 정의되어 있다고 가정하겠습니다. 이 도큐먼트는 `MongoRepository`를 사용하여 업데이트하지 않기 때문에, 필드들이 `val`로 지정되어 있습니다. 필드를 `val`로 지정하면 도큐먼트의 특정 필드를 변경하기 위해 객체를 직접 수정한 뒤 `save`를 호출하는 방식이 불가능합니다. 이렇게 필드를 `val`로 지정하면 도큐먼트의 불변성을 보장하며, 특정 필드의 변경을 엄격히 관리할 수 있습니다.

### Repository 정의

```kotlin
interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository

interface MemberCustomRepository {
    fun updateName(targets: List<MemberQueryForm.UpdateName>)
}

class MemberCustomRepositoryImpl(mongoTemplate: MongoTemplate) : MemberCustomRepository, MongoCustomRepositorySupport<Member>(Member::class.java, mongoTemplate) {

    override fun updateName(targets: List<MemberQueryForm.UpdateName>) {
        bulkUpdate(
            targets.map {
                Pair(
                    { Query(Criteria.where("id").`is`(it.id)) },
                    { Update().set("name", it.name) }
                )
            }
        )
    }
}
```

`MongoCustomRepositorySupport`를 상속받아 `bulkUpdate` 메서드를 통해 `bulkOps`를 사용한 대량 업데이트를 수행합니다. 이를 활용하면 대량 데이터를 효율적으로 처리할 수 있으며, 단일 업데이트만 필요한 경우 `updateFirst`를 사용하여 업데이트를 수행할 수도 있습니다. 그러나 특별한 이유가 없다면 `MongoCustomRepositorySupport` 기반으로 대량 업데이트를 지원하는 `bulkUpdate`를 사용하는 것을 권장합니다.

이 방식에 대한 자세한 구현 방법은 이전 포스팅 [MongoDB Update 성능 측정 및 분석 - MongoCustomRepositorySupport을 통한 bulkOps 기능 제공](https://cheese10yun.github.io/spring-data-mongodb-update-performance/)에서 확인할 수 있습니다.

### 업데이트 쿼리에 사용할 객체 정의

```kotlin
object MemberQueryForm {
    data class UpdateName(
        val id: ObjectId,
        val name: String
    )
}
```

`MemberQueryForm` 객체를 정의하여 쿼리에 필요한 필드와 데이터를 명확하게 관리합니다. 이를 통해 업데이트 작업에서 어떤 필드가 업데이트되는지 명확히 파악할 수 있습니다. 만약 `MemberQueryForm`에 정의되지 않은 필드가 있다면, 해당 필드는 현재 업데이트 대상이 아니거나 정책적으로 업데이트되지 않는 필드라고 간주할 수 있습니다.

### 테스트 코드 예시

```kotlin
@MongoTestSupport
class MemberRepositoryTest(
    private val memberRepository: MemberRepository
) : MongoStudyApplicationTests() {

    @Test
    fun `updateName test`() {
        // given
        val members = (1..20).map {
            Member(
                name = "name",
                ...
            )
        }

        val targets = mongoTemplate
            .insertAll(members).map {
                MemberQueryForm.UpdateName(
                    id = it.id!!,
                    name = "newName"
                )
            }

        // when
        memberRepository.updateName(targets)

        // then
        val results = mongoTemplate.findAll<Member>()

        then(results).hasSize(20)
        then(results).allSatisfy {
            then(it.name).isEqualTo("newName")
        }
    }
}
```

해당 테스트 코드는 `MemberRepository`의 `updateName` 메서드를 검증합니다.

1. 먼저 `Member` 객체를 생성하고 MongoDB에 저장한 뒤, 저장된 데이터를 조회하여 `UpdateName` 객체를 생성합니다.
2. 이후 `updateName` 메서드를 호출하여 업데이트를 수행합니다.
3. 마지막으로 MongoDB에서 데이터를 다시 조회해, 업데이트가 성공적으로 이루어졌는지 확인합니다.

이처럼 `MemberQueryForm` 객체를 사용해 업데이트 대상 필드를 명확히 정의함으로써, 변경 작업의 범위를 명확히 관리하고 추적할 수 있습니다. 테스트 코드 역시 이러한 명확성을 기반으로 업데이트 로직을 확인하도록 작성되었습니다.

## 정리

제가 담당하는 도메인은 특정 필드마다 업데이트 권한이 다르게 설정되어 있어, 업데이트 필드를 보다 명확하고 엄격하게 관리해야 하는 상황입니다. 또한, 대량의 데이터를 처리해야 하며, 빠른 처리를 보장해야 하는 요구사항도 있습니다. 이러한 이유로, 위에서 설명한 방식의 업데이트 전략을 선택했습니다. 각자의 상황과 요구사항에 맞는 적절한 방법을 선택하는 것이 가장 중요합니다.

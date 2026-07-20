# Spring Data MongoDB 업데이트 가이드: 특정 필드 업데이트 전략과 성능 최적화

MongoDB의 업데이트 작업은 단순해 보이지만, **무엇을(어떤 필드를)** 업데이트하고 **어떻게(어떤 방식으로)** 업데이트하느냐에 따라 데이터 정합성과 성능이 크게 갈립니다. Spring Data MongoDB는 `repository.save`부터 `mongoTemplate.updateFirst`, `bulkOps`까지 다양한 업데이트 수단을 제공하는데, 각 방식의 특성을 이해하지 못한 채 가장 손에 익은 `save`만 사용하다 보면 의도치 않은 데이터 덮어쓰기나 심각한 성능 저하를 만나게 됩니다.

이번 포스팅에서는 Spring Data MongoDB의 업데이트 작업을 두 가지 축으로 나누어 정리합니다.

1. **특정 필드만 업데이트하는 전략** — `repository.save`의 구조적 위험을 짚어보고, 변경 대상 필드를 명시적으로 관리하는 방법을 소개합니다. (정확성·안전성)
2. **업데이트 성능 가이드** — `saveAll`, `updateFirst`, `updateMulti`, `bulkOps`의 성능을 측정·비교하고, Repository 확장 패턴과 단계적 성능 최적화 접근법을 소개합니다. (효율성)

## 왜 repository.save가 위험한가

### NoSQL 도큐먼트는 필드가 많아질 수밖에 없다

MongoDB 같은 NoSQL은 스키마가 매우 유연하고, 조인 대신 **역정규화(denormalization)** 로 데이터를 중복 저장하는 모델링이 일반적입니다. RDB라면 여러 테이블로 나뉘었을 데이터가 하나의 도큐먼트에 담기기 때문에, 컬렉션의 필드 수는 구조적으로 늘어날 수밖에 없습니다.

여기서 중요한 것은, 필드가 많다는 것이 단순히 저장할 데이터가 많다는 의미가 아니라는 점입니다. **필드가 많다는 것은 해당 컬렉션이 다루는 컨텍스트와 비즈니스 항목이 많다는 의미입니다.** 예를 들어 가맹점(merchant) 컬렉션을 생각해보겠습니다.

```kotlin
@Document(collection = "merchants")
class Merchant(
    // 가맹점 기본 정보 컨텍스트
    @Field(name = "name")
    val name: String,

    @Field(name = "business_number")
    val businessNumber: String,

    @Field(name = "address")
    val address: Address,

    // 정산 컨텍스트
    @Field(name = "commission_rate")
    val commissionRate: BigDecimal,

    @Field(name = "settlement_cycle")
    val settlementCycle: SettlementCycle,

    @Field(name = "bank_account")
    val bankAccount: BankAccount,

    // 상태 관리 컨텍스트
    @Field(name = "status")
    val status: MerchantStatus,

    @Field(name = "suspended_at")
    val suspendedAt: LocalDateTime?,

    // ... 그 외 다수의 필드
) : Auditable()
```

하나의 도큐먼트 안에 가맹점 기본 정보, 정산·수수료 정보, 상태 관리라는 서로 다른 컨텍스트가 함께 존재합니다. 각 컨텍스트는 담당하는 팀도, 변경이 일어나는 업무 흐름도, 요구되는 변경 엄격성도 다릅니다. 가맹점 이름은 운영자가 자유롭게 수정할 수 있지만, 수수료율은 정산 도메인에서 엄격한 승인 절차를 거쳐야만 변경되어야 하는 필드입니다.

### save는 모든 필드가 모든 곳에서 변경 가능한 구조를 만든다

이렇게 다양한 컨텍스트가 혼재된 도큐먼트를 모두 `repository.save`로 업데이트한다면 어떻게 될까요. `save`는 객체의 전체 상태를 저장하는 메서드이기 때문에, **`save`를 호출하는 모든 코드 구간에서 모든 필드가 업데이트될 수 있는 구조**가 됩니다.

가맹점 이름을 변경하는 로직이든, 정산 수수료를 변경하는 로직이든, 상태를 변경하는 로직이든 결국 같은 `save`를 호출합니다. 코드만 보고 "이 지점에서는 어떤 필드가 변경되는가"를 파악할 수 없고, 반대로 "수수료 필드는 어디에서 변경될 수 있는가"를 추적하려면 `save` 호출부 전체를 열어 객체가 어떻게 조작되었는지 일일이 따라가야 합니다. 필드와 컨텍스트가 늘어날수록 이 추적 비용은 기하급수적으로 커지고, 복잡한 컨텍스트 관리를 더욱 악화시킵니다.

수수료처럼 데이터 변경에 엄격해야 하는 도메인이라면 이는 치명적인 단점입니다. 정산 금액에 직접 영향을 주는 필드가 "이론상 어디서든 변경 가능한" 상태로 열려 있는 것이기 때문입니다.

### 동시성 시나리오: 의도치 않은 덮어쓰기

`save` 기반 업데이트의 문제는 코드 추적의 어려움에서 끝나지 않습니다. 동시성 환경에서는 실제 데이터 유실로 이어집니다. 동일한 가맹점 도큐먼트에 대해 두 사용자가 서로 다른 필드를 수정하는 상황을 가정해보겠습니다.

- 사용자 A: 가맹점 이름을 `A상점 → B상점`으로 변경
- 사용자 B: 수수료율을 `2% → 3%`로 변경

두 작업 모두 "조회 → 객체 수정 → save" 흐름으로 처리된다면 다음과 같은 타임라인이 발생할 수 있습니다.

| 시점 | 사용자 A                              | 사용자 B                          | DB 상태                    |
|----|------------------------------------|--------------------------------|--------------------------|
| t1 | 도큐먼트 조회 (이름: A상점, 수수료: 2%)         |                                | 이름: A상점, 수수료: 2%         |
| t2 |                                    | 도큐먼트 조회 (이름: A상점, 수수료: 2%)     | 이름: A상점, 수수료: 2%         |
| t3 |                                    | 수수료 3%로 변경 후 `save`            | 이름: A상점, **수수료: 3%**     |
| t4 | 이름 B상점으로 변경 후 `save`               |                                | **이름: B상점, 수수료: 2%** ⚠️  |

사용자 A는 이름만 바꿀 의도였지만, `save`는 t1 시점에 조회한 객체의 **전체 상태**를 저장합니다. A가 들고 있던 객체의 수수료는 여전히 2%이므로, t4의 `save`가 실행되는 순간 B가 변경한 수수료 3%는 조회 시점의 낡은 값(2%)으로 덮어써집니다. 전형적인 Lost Update이며, 서로 다른 필드를 수정했음에도 데이터가 유실된다는 점에서 더 위험합니다. 에러도 발생하지 않기 때문에 정산 금액이 틀어진 뒤에야 문제를 인지하게 됩니다.

만약 각 작업이 **변경하려는 필드만** 업데이트했다면 어떨까요. A는 `name` 필드만, B는 `commission_rate` 필드만 `$set`으로 업데이트하면 두 변경은 서로 다른 필드를 건드리므로 실행 순서와 무관하게 둘 다 안전하게 반영됩니다. 이 문제는 락이나 버전 관리 같은 추가 장치 없이, **업데이트 범위를 명시하는 것만으로 구조적으로 사라집니다.**

이것이 본 포스팅에서 `repository.save` 대신 특정 필드만 업데이트하는 방식을 권장하는 이유입니다.

## 특정 필드만 업데이트하는 전략

### 업데이트 메서드 동작 방식 비교

Spring Data MongoDB에서 사용되는 주요 업데이트 메서드들은 아래와 같이 동작 방식과 적합한 시나리오에서 차이가 있습니다.

| **특징**             | **mongoRepository.save** | **mongoTemplate.save** | **mongoTemplate.updateFirst** |
|--------------------|--------------------------|------------------------|-------------------------------|
| **작업 대상**          | 단일 문서                    | 단일 문서                  | 단일 문서                         |
| **저장 방식**          | 전체 문서 교체                 | 전체 문서 교체               | 변경된 필드만 업데이트                  |
| **문서가 없을 경우**      | 새로 삽입                    | 새로 삽입                  | 기본적으로 아무 작업도 수행하지 않음          |
| **업데이트 범위**        | 전체 문서                    | 전체 문서                  | 필드 단위                         |
| **조건 지정**          | `_id` 기준                 | `_id` 기준               | 사용자 정의 쿼리                     |
| **쿼리 정의 방식**       | 도메인 인터페이스(파생 쿼리)         | 수동 쿼리(`Query`)         | 수동 쿼리(`Query`)                |
| **적합한 상황**         | 간단한 CRUD 작업              | 전체 문서 교체 또는 삽입         | 조건에 맞는 단일 문서 필드 수정            |

#### mongoTemplate.save — 문서 전체 교체(Replace)

`_id`를 기준으로 문서를 찾아 **전체 문서를 교체**합니다. 저장 객체에 없는 필드는 기존 문서에서 삭제됩니다.

```kotlin
val user = User(id = "123", name = "John Doe", age = 30)
mongoTemplate.save(user)
```

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe", "age": 30 }`
- 변경 사항: **`email` 필드가 삭제됨**

전체 교체라는 특성상 의도치 않은 필드 삭제로 이어지기 쉬워, 일반적인 업데이트 용도로는 거의 사용되지 않습니다.

#### mongoRepository.save — 전체 문서 교체(Replace)

`mongoRepository.save`는 이름과 달리 **부분 업데이트가 아닙니다.** 내부적으로 `MongoTemplate.save`를 그대로 호출하기 때문에, `mongoTemplate.save`와 **완전히 동일하게 전체 문서를 교체**합니다.

```kotlin
// SimpleMongoRepository 내부 구현 (요약)
override fun <S : T> save(entity: S): S {
    return if (entityInformation.isNew(entity)) {
        mongoOperations.insert(entity, ...)   // 신규면 insert
    } else {
        mongoOperations.save(entity, ...)     // 기존이면 결국 MongoTemplate.save() = replaceOne
    }
}
```

JPA의 dirty checking처럼 "변경된 필드만 추려서" 반영하는 메커니즘은 존재하지 않습니다. 저장 객체에 담기지 않은 필드는 기존 문서에서 삭제됩니다.

```kotlin
val user = User(id = "123", name = "John Doe") // age, email은 설정하지 않음
userRepository.save(user)
```

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe" }`
- 변경 사항: `name`은 교체되고, **설정하지 않은 `age`·`email`은 삭제됨** (앞의 `mongoTemplate.save`와 동일한 결과)

그렇다면 "조회 → 수정 → save" 흐름에서는 왜 나머지 필드가 유지되는 것처럼 보일까요. 그것은 MongoDB가 필드를 병합해주기 때문이 **아니라**, DB에서 로드한 객체가 이미 모든 필드를 들고 있어 save 시점에 그 값들이 그대로 다시 쓰이기 때문입니다. 즉 "필드가 유지된다"가 아니라 "낡은 값으로 다시 덮어쓴다"에 가깝습니다. 바로 이 성질이 앞 장에서 본 Lost Update의 원인입니다.

Spring Data JPA 경험이 있는 개발자에게 가장 익숙하고 직관적인 방식이지만, 이처럼 "객체 전체 상태 기준"으로 동작하기 때문에 변경 범위를 코드 레벨에서 통제할 수 없다는 구조적 한계가 있습니다.

#### mongoTemplate.updateFirst — 명시적 필드 업데이트

조건에 매칭된 첫 번째 문서의 **지정한 필드만** 업데이트합니다. 문서가 없으면 기본적으로 아무 작업도 수행하지 않습니다.

```kotlin
val query = Query(Criteria.where("name").`is`("Alice"))
val update = Update().set("age", 30)
mongoTemplate.updateFirst(query, update, User::class.java)
```

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "Alice", "age": 30, "email": "alice@example.com" }`
- 변경 사항: `age` 필드만 업데이트, 나머지 필드는 유지됨

### Update().set() 기반 명시적 필드 업데이트

`updateFirst`의 핵심은 `Update().set("age", 30)`처럼 **변경 대상 필드가 코드에 명시적으로 드러난다**는 점입니다. MongoDB의 `$set` 연산자를 사용해 지정된 필드만 원자적으로 갱신하므로, 이 코드가 `age` 외의 필드를 변경할 가능성은 없습니다.

`save` 방식과 비교하면 관점 자체가 다릅니다. `save`는 "객체의 현재 상태를 저장한다"는 관점이라 변경 범위가 암묵적이지만, `Update().set()`은 "이 필드를 이 값으로 바꾼다"는 관점이라 변경 범위가 명시적입니다. 코드 리뷰 시점에 변경 범위가 그대로 보이고, 앞서 살펴본 동시성 덮어쓰기 문제도 발생하지 않습니다.

### QueryForm(DTO) 패턴: 업데이트 필드를 한곳에서 관리

`Update().set()`으로 변경 필드를 명시하더라도, 업데이트 로직이 여러 서비스에 흩어져 있다면 "어떤 필드가 어디에서 업데이트되는가"라는 질문에는 여전히 코드 전체를 뒤져야 합니다. 그래서 업데이트에 사용할 필드들을 **별도의 DTO 클래스로 정의하고 한곳에서 관리**하는 방식을 함께 사용합니다.

```kotlin
object MerchantQueryForm {
    data class UpdateName(
        val id: ObjectId,
        val name: String
    )

    data class UpdateCommissionRate(
        val id: ObjectId,
        val commissionRate: BigDecimal
    )
}
```

`MerchantQueryForm`은 Merchant 컬렉션에 대한 업데이트 명세서 역할을 합니다. 이 패턴의 효과는 다음과 같습니다.

- **업데이트 카탈로그**: `MerchantQueryForm` 클래스 하나만 보면 이 컬렉션에 어떤 종류의 업데이트가 존재하는지, 각 업데이트가 어떤 필드를 다루는지 한눈에 파악할 수 있습니다.
- **정책의 코드화**: `UpdateName`에는 `name`만 담겨 있으므로, 이름 변경 작업이 수수료율이나 상태 필드를 건드릴 방법이 타입 수준에서 차단됩니다. 반대로 QueryForm에 정의되지 않은 필드는 "현재 업데이트 대상이 아니거나 정책적으로 변경하지 않는 필드"라고 간주할 수 있습니다.
- **추적 용이성**: 특정 필드가 어디에서 변경되는지 알고 싶다면 해당 QueryForm의 사용처만 추적하면 됩니다.

여기에 더해 도큐먼트 필드를 `val`로 선언하면 이 전략이 더 견고해집니다. 앞서 살펴본 `Merchant` 도큐먼트를 다시 보겠습니다.

```kotlin
@Document(collection = "merchants")
class Merchant(
    @Field(name = "name")
    val name: String,

    @Field(name = "business_number")
    val businessNumber: String,

    @Field(name = "commission_rate")
    val commissionRate: BigDecimal,

    @Field(name = "status")
    val status: MerchantStatus,

    // ... 그 외 필드 생략
) : Auditable()
```

필드가 `val`이므로 "조회한 객체의 필드를 수정한 뒤 `save`"하는 경로 자체가 컴파일 수준에서 불가능합니다. 도큐먼트의 불변성이 보장되고, 모든 업데이트는 QueryForm을 거쳐 명시적인 업데이트 쿼리로만 수행되도록 강제됩니다.

> 엄밀히는 `data class`라면 `copy()`로 일부 필드만 바꾼 새 인스턴스를 만들어 `save`하는 우회 경로가 남습니다. 위 예시처럼 일반 `class`로 선언하면 `copy()`가 없어 이 경로까지 막을 수 있습니다. 즉 `val` 선언은 "기존 인스턴스의 제자리 수정"을 막는 1차 방어선이고, 일반 `class` 사용은 그 방어선을 한층 더 굳히는 선택입니다.

### Repository 구성 예시

이제 QueryForm을 받아 실제 업데이트를 수행하는 Repository를 구성해보겠습니다. Spring Data JPA에서 Custom Repository를 확장하는 것과 동일한 패턴입니다.

```kotlin
interface MerchantRepository : MongoRepository<Merchant, ObjectId>, MerchantCustomRepository

interface MerchantCustomRepository {
    fun updateName(targets: List<MerchantQueryForm.UpdateName>)
}

class MerchantCustomRepositoryImpl(mongoTemplate: MongoTemplate) :
    MerchantCustomRepository,
    MongoCustomRepositorySupport<Merchant>(Merchant::class.java, mongoTemplate) {

    override fun updateName(targets: List<MerchantQueryForm.UpdateName>) {
        bulkUpdate(
            targets.map {
                Pair(
                    { Query(Criteria.where("_id").`is`(it.id)) },
                    { Update().set("name", it.name) }
                )
            }
        )
    }
}
```

`updateName`은 `MerchantQueryForm.UpdateName` 리스트를 받아 각 대상의 `name` 필드만 업데이트합니다. 시그니처만 봐도 "이 메서드는 name 필드를 업데이트한다"는 사실이 명확하게 드러납니다. 내부에서 사용하는 `bulkUpdate`는 `MongoCustomRepositorySupport`가 제공하는 벌크 업데이트 편의 메서드로, 자세한 구현은 다음 장에서 다룹니다.

테스트 코드로 동작을 검증해보겠습니다.

```kotlin
@MongoTestSupport
class MerchantRepositoryTest(
    private val merchantRepository: MerchantRepository
) : MongoStudyApplicationTests() {

    @Test
    fun `updateName test`() {
        // given
        val merchants = (1..20).map {
            Merchant(
                name = "name",
                ...
            )
        }

        val targets = mongoTemplate
            .insertAll(merchants).map {
                MerchantQueryForm.UpdateName(
                    id = it.id!!,
                    name = "newName"
                )
            }

        // when
        merchantRepository.updateName(targets)

        // then
        val results = mongoTemplate.findAll<Merchant>()

        then(results).hasSize(20)
        then(results).allSatisfy {
            then(it.name).isEqualTo("newName")
        }
    }
}
```

`Merchant` 객체를 저장한 뒤 `UpdateName` QueryForm을 만들어 `updateName`을 호출하고, 다시 조회하여 `name` 필드만 의도대로 변경되었는지 확인합니다. 업데이트 대상 필드가 QueryForm으로 명확히 정의되어 있기 때문에, 테스트 역시 검증해야 할 범위가 명확해집니다.

## 업데이트 성능 가이드

지금까지는 "무엇을 업데이트할 것인가"를 다뤘다면, 이제는 "어떻게 빠르게 업데이트할 것인가"를 다룰 차례입니다. 특히 대량의 문서를 업데이트하는 배치성 작업에서는 어떤 메서드를 선택하느냐에 따라 성능이 수십 배 차이 납니다.

### 업데이트 방식별 동작과 특성

#### saveAll

```kotlin
fun updateSaveAll(merchants: List<Merchant>) {
    // name 필드만 UUID.randomUUID().toString() 으로 업데이트
    merchantRepository.saveAll(merchants)
}
```

`saveAll`은 `CrudRepository`가 기본 제공하는 메서드로, 리스트를 순회하며 각 객체의 `id` 존재 여부에 따라 삽입 또는 업데이트를 수행합니다. 별도의 학습 없이 바로 사용할 수 있다는 것이 큰 장점이지만, 내부 동작을 들여다보면 리스트를 순회하면서 **문서 한 건마다 독립적인 DB I/O가 발생**합니다. N건을 업데이트한다면 네트워크 왕복도 N회 일어난다는 뜻입니다. 또한 변경되지 않은 필드까지 문서 전체를 전송하기 때문에 payload 측면에서도 비효율적입니다. "기본 제공이라 무심코 사용했다가" 대량 처리에서 성능 이슈를 만나는 대표적인 사례입니다.

#### updateFirst

```kotlin
fun updateFirst(id: ObjectId): UpdateResult {
    return mongoTemplate.updateFirst(
        Query(Criteria.where("_id").`is`(id)),
        Update().set("name", UUID.randomUUID().toString()),
        Merchant::class.java
    )
}
```

`updateFirst`는 변경할 필드만 명시적으로 지정할 수 있어 payload 면에서는 `saveAll`보다 효율적입니다. 하지만 **DB I/O 메커니즘은 `saveAll`과 동일**합니다. 호출 한 번이 곧 네트워크 왕복 한 번이므로, N건을 업데이트하려면 외부에서 루프를 돌며 N번 호출해야 합니다. 결과적으로 두 방식은 성능 특성상 같은 한계를 공유합니다. 또한 `_id` 외 다른 필드로 조회할 경우 인덱스 상태에 따라 매칭 비용이 추가되어 성능 차이가 더 벌어질 수 있습니다.

#### updateMulti

```kotlin
fun updateStatusBulk(targetIds: List<ObjectId>, status: MerchantStatus): Long {
    return mongoTemplate.updateMulti(
        Query(Criteria.where("_id").`in`(targetIds).and("status").ne(status)),
        Update().set("status", status),
        Merchant::class.java
    ).modifiedCount
}
```

`updateMulti`는 조건에 매칭되는 **여러 문서를 단일 쿼리 한 번으로** 동일한 값으로 업데이트합니다. 가장 빛을 발하는 사례는 **가맹점 상태(영업/정지)** 처럼 업데이트하려는 값의 종류가 한정적인 경우입니다. 100건의 가맹점 상태를 변경해야 한다면, 대상 ID 목록을 영업과 정지 두 그룹으로 나누어 `updateMulti`를 최대 2회 호출하면 됩니다. `saveAll`이나 `updateFirst`로 처리했다면 100회 발생했을 DB I/O가 최대 2회로 줄어듭니다.

단, `updateMulti`는 **모든 대상 문서에 동일한 값을 써야 한다는 제약**이 있습니다. 가맹점별 수수료율 변경처럼 각 문서마다 set해야 하는 값이 다를 경우에는 이 메서드 하나로 처리할 수 없습니다. 이런 케이스가 바로 다음에 소개하는 `bulkOps`가 필요한 이유입니다.

#### bulkOps

```kotlin
fun updateBulk(
    ids: List<ObjectId>,
    bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED // or BulkOperations.BulkMode.ORDERED
): BulkWriteResult {
    val bulkOps = mongoTemplate.bulkOps(bulkMode, Merchant::class.java)
    for (id in ids) {
        bulkOps.updateOne(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("name", UUID.randomUUID().toString())
        )
    }
    return bulkOps.execute()
}
```

`bulkOps`의 핵심도 `updateMulti`와 마찬가지로 **DB I/O 횟수를 줄이는 것**이지만, 접근 방식이 다릅니다. 각 문서마다 다른 값을 set해야 하는 경우 `updateMulti` 한 번으로는 묶을 수 없어 결국 N회 I/O가 발생합니다. `bulkOps`는 update 명령들을 **클라이언트 측 버퍼에 누적**하다가 `execute()` 시점에 묶어 전송합니다. 즉, 각기 다른 값을 N건 업데이트하더라도 실제 DB로 나가는 네트워크 왕복은 건별 N회가 아니라 **소수의 배치**로 크게 줄어듭니다.

> MongoDB 드라이버는 하나의 bulk write를 `maxWriteBatchSize`(현대 버전 기준 100,000 ops)와 메시지 크기 한도(약 48MB)에 맞춰 **자동으로 배치 분할**합니다. 따라서 왕복 횟수는 정확히는 `ceil(N / 배치 크기)`이며, 작은 문서를 다루는 대부분의 경우 한 번 또는 소수의 왕복으로 수렴합니다. "N회 → 1회"는 이 한도 안에 들어오는 경우의 근사 표현으로 이해하면 됩니다.

정리하면 두 메서드의 선택 기준은 다음과 같습니다.

- **동일한 값을 다수 문서에 적용** → `updateMulti`: 조건 쿼리만으로 한 번에 처리
- **문서마다 다른 값을 적용** → `bulkOps`: 각 update 명령을 버퍼에 쌓고 `execute()` 시점에 일괄 전송

`bulkOps`는 실행 모드를 선택할 수 있습니다.

- **`BulkMode.UNORDERED`**: 작업들이 순서에 구애받지 않고 처리됩니다. 하나의 작업 실패가 다른 작업에 영향을 미치지 않으며, 대량의 독립적인 작업을 빠르게 처리해야 할 때 유용합니다.
- **`BulkMode.ORDERED`**: 작업들이 추가된 순서대로 처리됩니다. 하나의 작업이 실패하면 그 이후의 작업은 실행되지 않을 수 있으며, 작업 간 순서가 중요한 경우에 적합합니다.

### 성능 측정 결과

성능 테스트는 `saveAll`, `updateFirst`, `bulkOps(UNORDERED)`, `bulkOps(ORDERED)`를 대상으로, 문서마다 서로 다른 값을 set하는 워크로드에서 진행했습니다.

`updateMulti`는 이번 벤치마크에서 의도적으로 제외했습니다. 첫째, `updateMulti`는 단일 쿼리 한 번으로 다수 문서를 처리하므로 N 값에 관계없이 사실상 상수에 가까운 처리 시간을 보여, "행 수에 따른 시간 변화"를 측정하는 다른 방식들과 같은 축에 놓으면 비교 자체가 의미를 잃습니다. 둘째, `updateMulti`는 모든 대상에 동일한 값을 적용하는 시나리오에만 사용 가능하므로, 각 문서마다 다른 값을 set하는 이번 워크로드와 동일한 조건으로 테스트를 재현할 수 없습니다.

![](MongoDB%20Update%20Performance.svg)


| rows   | saveAll    | updateFirst | bulkOps(UNORDERED) | bulkOps(ORDERED) |
|--------|------------|-------------|--------------------|------------------|
| 100    | 1,052 ms   | 1,176 ms    | 46 ms              | 79 ms            |
| 200    | 2,304 ms   | 2,196 ms    | 103 ms             | 124 ms           |
| 500    | 5,658 ms   | 5,250 ms    | 309 ms             | 257 ms           |
| 1,000  | 11,106 ms  | 10,846 ms   | 418 ms             | 412 ms           |
| 2,000  | 22,592 ms  | 21,427 ms   | 1,060 ms           | 1,004 ms         |
| 5,000  | 54,407 ms  | 52,075 ms   | 2,663 ms           | 2,292 ms         |
| 10,000 | 107,651 ms | 110,884 ms  | 4,514 ms           | 4,496 ms         |

분석 결과는 다음과 같습니다.

**saveAll과 updateFirst**는 유사한 성능을 보이며, 행 수가 증가함에 따라 수행 시간이 선형적으로 증가합니다. 두 방식의 성능 차이는 유의미하지 않으므로, 데이터 양이 적은 경우에는 upsert 기능을 제공하는 `saveAll`로 로직을 단순화하는 것도 선택지가 될 수 있습니다. 단, 이번 테스트는 기본 키(PK) 기반으로 업데이트를 수행했기 때문에, 다른 키로 조회할 경우 인덱스 상태에 따라 성능 차이가 발생할 수 있습니다.

**bulkOps(UNORDERED)와 bulkOps(ORDERED)**는 `saveAll`·`updateFirst` 대비 현저히 빠른 성능을 보입니다. 10,000건 기준 약 107초 vs 4.5초, **약 24배 차이**입니다. 두 모드 간에는 10,000건까지는 큰 성능 차이가 나타나지 않았지만, 데이터가 여러 노드에 분산 저장된 환경에서는 순서 보장 비용으로 인해 두 방식의 차이가 더 명확하게 드러날 수 있습니다.

여기서 얻을 수 있는 가장 중요한 인사이트는, **복잡한 병렬 처리나 멀티스레드 같은 기법 없이 네트워크 I/O를 모아서 보내는 것만으로도** 성능 향상의 폭이 매우 크다는 점입니다. `bulkOps`가 24배 빠른 이유는 알고리즘이 우월해서가 아니라, 단순히 네트워크 왕복 횟수를 건별 N회에서 소수의 배치로 줄였기 때문입니다.

### MongoCustomRepositorySupport: JPA처럼 Repository 확장하기

`bulkOps`를 직접 사용할 때마다 반복되는 패턴을 생각해보면, 매번 `mongoTemplate.bulkOps(bulkMode, documentClass)`로 초기화하고, 루프를 돌며 `updateOne`으로 작업을 추가한 뒤, 마지막에 `execute()`를 호출하는 세 단계가 항상 고정적입니다. JPA에서 `QuerydslRepositorySupport` 같은 추상 클래스로 Repository를 확장하듯, MongoDB에서도 이 보일러플레이트를 추상 클래스에 한 번만 구현해두면 각 Repository 구현체는 핵심 비즈니스 로직인 `Query`와 `Update` 조합만 정의하면 됩니다.

```kotlin
abstract class MongoCustomRepositorySupport<T>(
    protected val documentClass: Class<T>,
    protected val mongoTemplate: MongoTemplate
) {

    protected fun bulkUpdate(
        operations: List<Pair<() -> Query, () -> Update>>,
        bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED
    ): BulkWriteResult {
        val bulkOps = mongoTemplate.bulkOps(bulkMode, documentClass)
        operations.forEach { (queryCreator, updateCreator) ->
            bulkOps.updateOne(queryCreator.invoke(), updateCreator.invoke())
        }
        return bulkOps.execute()
    }

    protected fun updateMany(criteria: Criteria, update: Update): Long {
        val query = Query(criteria)
        return mongoTemplate.updateMulti(query, update, documentClass).modifiedCount
    }
}
```

`bulkUpdate`는 `Pair<() -> Query, () -> Update>` 형태의 람다 리스트를 받습니다. 다만 이 람다 래핑이 실행 시점을 늦추는 등의 성능적 이점을 주지는 않습니다. `bulkUpdate` 내부의 `forEach`에서 곧바로 `invoke()`되므로, 사실상 `Pair<Query, Update>`를 직접 넘기는 것과 동작이 동일합니다. 여기서 람다를 사용한 이유는 호출부에서 `{ Query(...) } to { Update(...) }`처럼 조건과 변경 내용을 선언적으로 나열해 가독성을 높이려는 스타일 선택에 가깝습니다. 단순함을 우선한다면 `Pair<Query, Update>` 형태로 두어도 무방합니다.

`updateMany`는 `updateMulti`를 감싸는 래퍼로, 동일한 값을 다수 문서에 일괄 적용하는 시나리오에서 사용합니다. `bulkUpdate`와 함께 추상 클래스에 포함시켜두면, 어느 Repository든 상속만으로 두 메서드를 모두 활용할 수 있습니다.

구현체에서의 활용 예시입니다.

```kotlin
class MerchantCustomRepositoryImpl(mongoTemplate: MongoTemplate) :
    MerchantCustomRepository,
    MongoCustomRepositorySupport<Merchant>(Merchant::class.java, mongoTemplate) {

    // 가맹점별로 서로 다른 수수료율을 업데이트 — 문서마다 값이 다르므로 bulkUpdate 사용
    override fun updateCommissionRates(targets: List<MerchantQueryForm.UpdateCommissionRate>): BulkWriteResult {
        val operations = targets.map {
            Pair(
                first = { Query(Criteria.where("_id").`is`(it.id)) },
                second = { Update().set("commission_rate", it.commissionRate) }
            )
        }
        return bulkUpdate(operations)
    }

    // 특정 가맹점들을 일괄 정지 — 모두 동일한 값이므로 updateMany 사용
    override fun suspendMerchants(targetIds: List<ObjectId>): Long {
        return updateMany(
            Criteria.where("_id").`in`(targetIds),
            Update().set("status", MerchantStatus.SUSPENDED)
        )
    }
}
```

두 메서드를 나란히 놓고 보면 앞서 설명한 선택 기준이 코드 수준에서도 명확하게 드러납니다. `updateCommissionRates`는 가맹점마다 적용할 수수료율이 다르기 때문에 `bulkUpdate`로 각 update 명령을 버퍼에 쌓아 한 번에 전송하고, `suspendMerchants`는 모든 대상에 동일한 `SUSPENDED` 상태를 적용하므로 `updateMany`로 단일 쿼리 한 번에 처리합니다. 두 경우 모두 `MongoCustomRepositorySupport`가 내부 구현을 캡슐화하고 있어, Repository 구현체에는 비즈니스 의도를 명확하게 표현하는 코드만 남습니다.

이 패턴은 앞 장에서 소개한 QueryForm 전략과 자연스럽게 결합됩니다. QueryForm으로 "무엇을 업데이트하는지"를 명세하고, `MongoCustomRepositorySupport`로 "어떻게 효율적으로 업데이트하는지"를 캡슐화하면, 정확성과 성능을 모두 갖춘 업데이트 계층이 완성됩니다.

### 성능 최적화의 순서: 단순한 것부터

성능 개선이 필요할 때 병렬 처리, 멀티스레드, 비동기 같은 기법부터 떠올리기 쉽습니다. 하지만 위 측정 결과가 보여주듯, **특별한 기법 없이 단순하게 I/O만 몰아서 처리해도 성능적인 이점이 매우 큽니다.** 성능 최적화에는 단계가 있으며, 직관적이고 유지보수가 쉬운 방법부터 순서대로 적용해보고 그래도 부족하다면 다음 단계로 넘어가는 접근이 디버깅·운영 비용을 포함한 총비용을 가장 낮춥니다.

1. **데이터 양이 적고 단순한 경우** → `saveAll` / `updateFirst`로 충분, 코드 단순성을 우선
2. **같은 값을 다수 문서에 일괄 적용하는 경우** → `updateMulti`로 I/O를 "유니크한 값 수"로 수렴
3. **문서마다 다른 값을 적용해야 하는 경우** → `bulkOps`로 I/O를 1회로 수렴
4. **위 방법으로도 성능이 부족한 경우** → 그제야 청크 분할, 비동기/병렬 처리, 샤딩 키 설계 등 다음 단계로 진입

복잡한 기법일수록 코드 가독성이 떨어지고 운영 중 문제 발생 시 추적이 어렵습니다. 처음부터 직관적이지 않은 방법으로 시작하면 성능은 얻더라도 디버깅과 운영에서 더 큰 곤경에 처할 수 있습니다. 동기적이고 직관적인 방향으로 성능 개선의 여지가 있는지 먼저 확인하고, 그 이후에도 더 끌어올려야 한다면 적절한 다음 방법을 찾는 것이 바람직합니다.

## 마무리

Spring Data MongoDB의 업데이트 전략을 정확성과 성능이라는 두 축으로 정리했습니다.

- **정확성**: 다양한 컨텍스트가 혼재된 도큐먼트를 `repository.save`로 업데이트하면 변경 범위를 통제할 수 없고, 동시성 환경에서는 의도치 않은 덮어쓰기로 이어집니다. `Update().set()` 기반의 명시적 필드 업데이트와 QueryForm(DTO) 패턴으로 "어떤 필드가 어디에서 업데이트되는지"를 코드 레벨에서 명확하게 관리할 수 있습니다.
- **성능**: 대량 업데이트에서는 네트워크 I/O 횟수가 성능을 지배합니다. 동일한 값 적용은 `updateMulti`, 문서마다 다른 값 적용은 `bulkOps`로 I/O를 수렴시키고, 이러한 반복 패턴은 `MongoCustomRepositorySupport` 같은 추상 클래스로 캡슐화하여 JPA처럼 Repository를 확장하는 방식으로 재사용할 수 있습니다.

제가 담당하는 도메인은 특정 필드마다 업데이트 권한이 다르게 설정되어 있어 업데이트 필드를 명확하고 엄격하게 관리해야 하고, 동시에 대량의 데이터를 빠르게 처리해야 하는 요구사항도 있어 이러한 전략을 선택했습니다. 모든 프로젝트에 정답인 방식은 아니며, 각자의 도메인이 요구하는 변경 엄격성과 데이터 양에 맞는 적절한 방법을 선택하는 것이 가장 중요합니다.

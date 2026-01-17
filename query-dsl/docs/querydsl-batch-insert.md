# QueryDSL을 이용한 Batch Insert 성능 개선

JPA를 사용하다 보면 대량의 데이터를 삽입해야 하는 상황에서 `saveAll`의 성능 한계에 부딪히게 됩니다. 이번 포스팅에서는 JPA `saveAll`의 성능 이슈를 살펴보고, QueryDSL의 `SQLQueryFactory`를 활용한 Batch Insert로 성능을 획기적으로 개선하는 방법을 소개합니다.

## 목차

1. [개요](#1-개요)
2. [JPA saveAll의 성능 이슈](#2-jpa-saveall의-성능-이슈)
3. [QueryDSL Batch Insert 구현](#3-querydsl-batch-insert-구현)
4. [성능 비교](#4-성능-비교)
5. [결론](#5-결론)

---

## 1. 개요

대량의 데이터를 데이터베이스에 저장해야 할 때, 일반적으로 JPA의 `saveAll` 메서드를 사용합니다. 하지만 데이터의 양이 늘어날수록 `saveAll`의 처리 속도는 급격히 느려질 수 있습니다. 특히 ID 생성 전략이 `IDENTITY`인 경우, JPA는 Batch Insert를 지원하지 않아 단건으로 Insert 쿼리가 실행되는 문제가 있습니다. 이를 해결하기 위해 JDBC의 `addBatch` 기능을 활용할 수 있는데, QueryDSL-SQL을 사용하면 이를 보다 편리하게 구현할 수 있습니다.

## 2. JPA saveAll의 성능 이슈

JPA(Hibernate)는 엔티티의 ID 생성 전략이 `@GeneratedValue(strategy = GenerationType.IDENTITY)`로 설정되어 있을 때, JDBC 레벨의 Batch Insert를 비활성화합니다. 이는 영속성 컨텍스트가 엔티티를 관리하기 위해 Insert 즉시 ID 값을 알아야 하기 때문입니다. 결과적으로 1,000개의 데이터를 저장하면 1,000번의 Insert 쿼리가 데이터베이스로 전송되어 성능 저하의 주원인이 됩니다.

## 3. QueryDSL Batch Insert 구현

QueryDSL-SQL 모듈을 사용하면 JPA 엔티티가 아닌 JDBC 레벨에서 직접 SQL을 구성하여 실행할 수 있습니다. 이를 통해 `addBatch` 기능을 활용한 Bulk Insert를 구현할 수 있습니다.

### 의존성 설정

먼저 `querydsl-sql` 관련 의존성이 필요합니다.

### 구현 코드 예시

`SQLQueryFactory`를 사용하여 Batch Insert를 구현하는 방법은 다음과 같습니다.

```kotlin
@Service
class BatchInsertService(
    private val dataSource: DataSource
) {
    @Transactional
    fun executeBulkInsertWithSql(members: List<Member>): Long {
        // 테이블 메타데이터 정의
        val memberTable = RelationalPathBase(Member::class.java, "member", null, "member")

        // 컬럼 정의
        val username = Expressions.stringPath(memberTable, "username")
        val age = Expressions.numberPath(Int::class.java, memberTable, "age")
        val status = Expressions.stringPath(memberTable, "status")
        val teamId = Expressions.numberPath(Long::class.java, memberTable, "team_id")

        // SQLQueryFactory 생성
        val sqlQueryFactory = SQLQueryFactory(Configuration(MySQLTemplates()), dataSource)

        val insert = sqlQueryFactory.insert(memberTable)

        for (member in members) {
            insert.set(username, member.username)
            insert.set(age, member.age)
            insert.set(status, member.status.name)
            insert.set(teamId, member.team.id)
            insert.addBatch() // Batch에 추가
        }

        return insert.execute() // 일괄 실행
    }
}
```

위 코드에서는 `RelationalPathBase`를 통해 테이블을 정의하고, `SQLQueryFactory`의 `insert`와 `addBatch`를 사용하여 쿼리를 메모리에 쌓은 뒤 `execute()`로 한 번에 전송합니다.

## 4. 성능 비교

JPA `saveAll`과 QueryDSL `addBatch`를 사용했을 때의 성능 차이를 비교한 결과입니다. 데이터 개수가 늘어날수록 성능 차이가 확연하게 벌어지는 것을 확인할 수 있습니다.

| rows   | saveAll (ms) | add batch (ms) | 성능 개선율 |
|:-------|:-------------|:---------------|:-------|
| 100    | 104          | 12             | 88.50% |
| 200    | 174.5        | 16             | 90.80% |
| 500    | 370.25       | 27.5           | 92.60% |
| 1,000  | 695          | 56             | 91.90% |
| 2,000  | 1,574        | 68             | 95.70% |
| 5,000  | 3,778        | 140            | 96.30% |
| 10,000 | 7,505        | 265            | 96.50% |

* **saveAll**: JPA Repository의 saveAll 메서드 사용
* **add batch**: QueryDSL SQLQueryFactory의 addBatch 사용

10,000건 기준으로 약 **96.5%**의 성능 개선 효과가 있었습니다. `saveAll`이 약 7.5초 걸리는 작업을 Batch Insert로는 0.26초 만에 처리할 수 있습니다.

## 5. 결론

대량의 데이터를 처리해야 하는 배치성 작업이나 초기 데이터 적재 시에는 JPA의 `saveAll`보다는 JDBC Batch Insert를 사용하는 것이 필수적입니다. QueryDSL-SQL을 활용하면 Type-Safe한 쿼리 작성의 장점을 누리면서도 JDBC의 성능 이점을 가져갈 수 있습니다. 프로젝트에서 대량 Insert가 필요한 구간이 있다면 꼭 적용해 보시기를 권장합니다.

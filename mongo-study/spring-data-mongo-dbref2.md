LookUp

| rows  | LookUp | DBRef lazy false | DBRef lazy true(author 접근) | DBRef lazy true(author 미접근) |
|-------|--------|:-----------------|:---------------------------|:----------------------------|
| 1     | 9.2ms  | 9.6ms            | 9.3ms                      | 8.5ms                       |
| 50    | 11.6ms | 69.7ms           | 69.4ms                     | 8.9ms                       |
| 100   | 16.2ms | 130.1ms          | 133.5ms                    | 11.5ms                      |
| 500   | 42.2ms | 574.2ms          | 575.9ms                    | 23.5ms                      |
| 1,000 | 69.5ms | 1167.4ms         | 1178.3ms                   | 41.9ms                      |

8.5
8.9
11.5
23.5
41.9

아래는 **“Spring Data MongoDB N+1 성능 측정과 해결방법”**의 서두 부분을 개선하여, **N+1 문제**가 무엇인지 간략히 설명하고, 이후 본문에서 다룰 내용들을 미리 안내하는 형태로 정리한 예시입니다.

---

# Spring Data MongoDB에서 N+1 문제 다루기: 성능 이슈와 최적화 전략

MongoDB에서 문서 간 연관관계를 처리할 때, **N+1 문제**가 발생할 수 있습니다. 이는 주로 **다대일(One-to-Many) 구조**에서, 한 번에 여러 문서를 조회한 뒤 각 문서마다 또 다른 문서를 개별 쿼리로 가져올 때 발생합니다. 예컨대, `Post` 문서 N개를 조회한 후, 각 `Post`마다 연관된 `Author` 문서를 별도 쿼리로 로딩한다면 **총 N+1개의 쿼리**가 실행되어 성능 저하가 발생하게 됩니다.

본 글에서는 아래와 같은 내용을 다룹니다.

1. **DBRef vs. ObjectId**
    - MongoDB에서 연관관계를 표현하는 대표적인 두 방식(DBRef, ObjectId 직접 참조)과 그 차이점
    - DBRef의 Lazy/Eager 로딩이 N+1 문제에 어떤 영향을 미치는지

2. **N+1 문제 발생 원리**
    - DBRef(Eager) 사용 시, 여러 문서를 한 번에 불러오면 각 문서마다 참조 문서를 추가 조회
    - DBRef(Lazy) 사용 시, 참조 필드에 실제 접근하는 시점마다 쿼리가 발생하여 예측이 어려움

3. **실제 성능 측정 결과**
    - DBRef(Eager), DBRef(Lazy), `$lookup` 등을 비교한 벤치마크
    - 대량의 문서를 조회할 때 어떤 방식이 유리한지, 실제 숫자로 확인

4. **해결 방법**
    - `$lookup`(Aggregation)으로 한 번에 조인하는 방식
    - ObjectId 직접 참조 후, 필요 시만 쿼리 실행
    - Slice, 병렬 처리 등으로 N+1을 완화하는 다양한 전략

이를 통해, Spring Data MongoDB 환경에서 **N+1 문제**를 어떻게 측정하고, 어떤 방식으로 최적화할 수 있는지 구체적인 예시와 함께 살펴보겠습니다. 이후 본문에서는 DBRef와 ObjectId 방식을 비교하고, 실제로 N+1 문제를 유발하는 시나리오와 성능 테스트 결과, 그리고 이를 개선하기 위한 다양한 방법들을 단계별로 소개합니다.

아래 표는 각 **rows** 값(1, 50, 100, 500, 1,000)에 대해 **10번씩 호출**하여 **평균 응답 시간**을 측정한 결과입니다. **LookUp**은 MongoDB의 `\$lookup` 단계를 사용하여 **Post**와 **Author**를 한 번의 쿼리로 조인한 방식이며, **DBRef lazy false**는 `@DBRef(lazy = false)` 설정을 통해 Post 조회 시 즉시 Author 문서를 로딩합니다. 한편, **DBRef lazy true(author 접근)는** `@DBRef(lazy = true)` 상태에서 Author 필드에 실제 접근할 때마다 추가 쿼리가 실행되는 구조이고, **DBRef lazy true(author 미접근)는** 같은 `@DBRef(lazy = true)`지만 Author 필드를 전혀 사용하지 않아 추가 쿼리가 발생하지 않는 상황을 의미합니다.
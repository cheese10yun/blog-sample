`AggregationOptions`는 Spring Data MongoDB의 Aggregation 파이프라인 실행 시 추가 옵션들을 설정할 수 있도록 해줍니다. `withOptions(options)`를 통해 적용할 수 있으며, 대표적으로 아래와 같은 메서드들을 사용할 수 있습니다.

| 메서드                          | 설명                                                                               |
|------------------------------|----------------------------------------------------------------------------------|
| **allowDiskUse(boolean)**    | 메모리가 부족할 때 디스크 사용을 허용할지 여부를 설정합니다. 대규모 정렬이나 그룹핑 시 유용합니다.                         |
| **cursorBatchSize(int)**     | 한 번에 가져올 문서(batch) 개수를 설정합니다. (대용량 데이터 처리 시 배치 크기 조정)                            |
| **comment(String)**          | MongoDB 쿼리에 코멘트를 달아 로그나 프로파일러에서 확인할 수 있게 합니다.                                    |
| **explainMode(ExplainMode)** | Aggregation 쿼리에 대해 실행 계획(`explain`) 정보를 확인할 수 있도록 설정합니다. (디버깅/튜닝용)               |
| **collation(Collation)**     | 문자열 비교 시 사용될 지역화(정렬/대소문자 구분 등)를 설정합니다.                                           |
| **hint(Document)**           | 인덱스 힌트를 지정하여 특정 인덱스를 사용하도록 MongoDB에 힌트를 줄 수 있습니다.                                |
| **maxTime(Duration)**        | Aggregation이 실행될 최대 시간을 설정합니다. 초과 시 타임아웃(예: `MongoExecutionTimeoutException`) 발생 |
| **maxAwaitTime(Duration)**   | 커서가 데이터 스트리밍 시 대기할 최대 시간을 설정합니다.                                                 |

### 예시 코드

```kotlin
val options = Aggregation.newAggregationOptions()
    .allowDiskUse(true)                // 대규모 정렬/그룹핑 시 디스크 사용 허용
    .cursorBatchSize(50)               // batchSize 설정
    .comment("My aggregation query")   // 코멘트 추가
    .explainMode(ExplainMode.QUERY_PLANNER) // 실행 계획 확인
    .build()

val aggregation = Aggregation.newAggregation(
    // ... 스테이지들 ...
).withOptions(options)

val results = mongoTemplate.aggregate(
    aggregation,
    "my_collection",
    MyResultType::class.java
).mappedResults
```

이처럼 **`AggregationOptions`**를 통해 디스크 사용 허용, 배치 크기, 코멘트, 실행 계획 확인 모드 등 다양한 설정을 제어할 수 있습니다.
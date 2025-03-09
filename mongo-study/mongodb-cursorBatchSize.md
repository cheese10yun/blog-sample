## 1. `cursorBatchSize`란 무엇인가?

- **의미**  
  Spring Data MongoDB의 AggregationOptions에서 `cursorBatchSize`는 MongoDB 서버가 한 번에 가져오는 **배치(batch)** 크기를 지정합니다.  
  즉, MongoDB 서버에서 한 번에 가져올 문서 개수를 제한함으로써, 네트워크 왕복과 메모리 사용량을 조절할 수 있습니다.

- **동작 방식**  
  `cursorBatchSize`를 설정하면, 내부적으로 MongoDB 드라이버가 서버에 “한 번에 이만큼씩 보내 달라”라고 요청하여, 클라이언트가 큰 데이터를 한 번에 다 불러오지 않고 여러 번에 걸쳐 나누어 받습니다.

---

## 2. `limit`, `skip`와의 차이점

- **`limit`**  
  Aggregation 파이프라인에서 `\$limit` 스테이지를 사용하면 **최종으로 가져올 문서 개수 자체**를 제한합니다.  
  반면, `cursorBatchSize`는 가져올 **총 개수**가 아니라, **한 번에 가져올 배치 단위**를 조절합니다.

- **`skip`**  
  `\$skip` 스테이지는 앞의 일부 문서를 건너뛰는 역할이며, `cursorBatchSize`와는 직접적인 연관은 없습니다.  
  `cursorBatchSize`는 문서 전송 단위(배치)에 관한 설정이므로, `skip`과 `limit` 같은 파이프라인 연산과는 역할이 다릅니다.

---

## 3. 성능 및 메모리 고려 사항

- **메모리 사용**  
  batchSize가 너무 크면, 클라이언트(애플리케이션) 측에서 한꺼번에 많은 문서를 로드해야 하므로 메모리 사용량이 급격히 늘어날 수 있습니다.  
  반면, batchSize가 너무 작으면, 왕복이 잦아져 **네트워크 오버헤드**가 늘어날 수 있습니다.

- **네트워크 왕복**  
  한 번에 가져오는 배치 크기가 커질수록 왕복 횟수는 줄어들지만, 한 번의 응답 덩어리가 커지므로 전송 시간이 길어질 수 있습니다.  
  따라서 적절한 batchSize 설정이 중요합니다.

- **예시**
    - 대량 데이터(수만~수십만 건)를 처리하는 상황에서 `cursorBatchSize`를 적절히 설정하면,
        - **OutOfMemory**와 같은 문제를 방지할 수 있고,
        - 클라이언트가 **Streaming** 방식으로 데이터를 처리하기 수월해집니다.

---

## 4. 실제 사용 시 주의사항

- **AggregationOptions와 함께 쓰는 상황**  
  `Aggregation.newAggregationOptions()`에서 `cursorBatchSize(2000)`를 지정하면, 실제로는 MongoDB의 커서(cursor) 기반으로 결과를 가져오게 됩니다.
    - Spring Data MongoDB가 내부적으로 AggregationCursor를 열고, 설정된 batchSize만큼씩 가져옵니다.

- **MongoDB 서버 버전**  
  특정 MongoDB 버전 이하에서는 Aggregation 파이프라인의 커서 옵션이 다를 수 있으므로, 사용 중인 서버 버전을 확인해야 합니다.

- **Spring Data MongoDB 버전**  
  `cursorBatchSize` 옵션이 추가된 버전(SDM 1.9 이후)에선 AggregationOptions와 함께 이 기능을 활용할 수 있습니다.  
  (버전에 따라 메서드나 옵션 이름이 다를 수 있으므로, 릴리스 노트를 참고하는 것이 좋습니다.)

- **기본 batchSize**  
  Spring Data MongoDB나 드라이버 레벨에서 기본 batchSize가 정해져 있을 수 있습니다(예: 101개).  
  별도 설정을 하지 않으면 해당 기본값이 적용됩니다.

---

## 5. 샘플 코드 및 사용 예시

```kotlin
val options = Aggregation.newAggregationOptions()
    .cursorBatchSize(2000)
    .build()

val aggregation = Aggregation.newAggregation(
    // 예: match, project, group 등 다양한 스테이지
).withOptions(options)

val results = mongoTemplate.aggregate(
    aggregation,
    "post",
    Post::class.java
).mappedResults
```

- **의미**: 위 코드에서는 Aggregation 파이프라인 결과를 **배치 단위 2000건**씩 나누어 가져옵니다.
- **적용 사례**: 대량의 `Post` 문서를 한 번에 조회해야 하지만, 메모리 사용량을 제어하고 싶을 때 유용합니다.

---

## 6. 실제 운영 시나리오 예시

1. **배치(ETL) 작업**  
   대량의 MongoDB 데이터를 한꺼번에 읽어 다른 저장소로 옮기는 ETL 시나리오에서, batchSize를 크게 설정해 네트워크 왕복 횟수를 줄이면서도 메모리를 과도하게 사용하지 않도록 조정할 수 있습니다.

2. **스트리밍 처리**  
   데이터 양이 매우 많을 때, 클라이언트에서 한 번에 모두 로드하지 않고, 일부씩 읽으면서 스트리밍 처리(예: Kafka 전송, 파일 쓰기)를 진행할 수 있습니다.

3. **페이징과의 병행**  
   클라이언트 측 페이징 UI를 구현하는 대신, 서버에서 커서를 유지하면서 일정 배치 단위로 클라이언트에 전송할 수도 있습니다.

---

## 7. 결론

`cursorBatchSize(2000)` 같은 설정은 MongoDB Aggregation 결과를 **배치 단위**로 가져올 때의 **성능과 메모리 사용**을 제어하기 위해 유용합니다.  
너무 큰 batchSize는 메모리 사용량이 커질 수 있고, 너무 작은 batchSize는 네트워크 왕복이 잦아질 수 있으므로, 실제 운영 환경에 맞춰 적절한 값을 테스트하며 조정하는 것이 중요합니다.  
**AggregationOptions**를 통해 세밀하게 커서 기반 쿼리를 제어할 수 있으니, 대규모 데이터 처리 시 꼭 고려해 보는 것을 권장합니다.
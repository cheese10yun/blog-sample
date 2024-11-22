# Java 기반 Producer 구현 실습 및 Producer 내부 메커니즘 이해 - 02

## acks 값 설정에 따른 Producer의 전송 방식 차이 이해

| 설정 값 (`acks`)   | 설명                                                                                             | 메시지 전송 신뢰도 | 성능 영향      |
|-----------------|------------------------------------------------------------------------------------------------|------------|------------|
| `0`             | 프로듀서는 브로커로부터의 응답을 기다리지 않습니다. 이 설정에서는 메시지가 브로커에 도달했는지 확인할 수 없으며, 신뢰도가 낮습니다.                     | 낮음         | 높음 (빠른 전송) |
| `1`             | 프로듀서는 리더 파티션으로부터의 확인을 기다립니다. 브로커가 메시지를 수신하고 기록하면 프로듀서에 응답을 보냅니다. 만약 리더가 다운되면 데이터 손실 위험이 존재합니다. | 중간         | 중간         |
| `all` (또는 `-1`) | 리더와 모든 팔로워(ISR, In-Sync Replica)로부터 확인을 기다립니다. 메시지가 모든 복제본에 기록될 때까지 응답을 기다리므로 신뢰도가 가장 높습니다.    | 높음         | 낮음 (성능 저하) |

## Producer의 메시지 배치 전송 내부 메커니즘 - Record Batch와 Record Accumulator 이해

### Producer의 메시지 배치 전송의 이해

![](/images/kafka-01.png)

* Producer는 메시지를 전송할 때, 단일 메시지가 아닌 배치로 전송합니다.
* 배치로 전송하는 이유는 네트워크 비용을 줄이고, 브로커의 디스크 I/O를 줄이기 위함입니다.
* broker로 전송하는 배치 단위로 보내게 되며, 별도의 쓰레드로 보내게 됩니다.

### Producer Record와 Record Batch

KafkaProducer 객체의 send() 메소드는 호출 시 마다 하나의 ProducerRecord를 입력하지만 바로 전송 되지 않고 내부 메모리에서 단일 메시지를 토픽 파티션에 따라 RecordBatch 단위로 묶인 뒤 전송됨. 메시지들은 Producer Client의 내부 매모리에 여러 개의 Batch로 buffer.memory 설정 사이즈 만큼 본관될 수 있으며 여러 개의 Batch들로 한꺼번에 전송될 수 있음

![](/images/kafka-02.png)

### Kafka Producer Record Accumulator

* Record Accumulator는 Partitioner에 의해서 메시지 배치가 전송이 될 토픽과 Partiotion에 따라 저장되는 KafkaProducer 메모리 영역
* Sender Thread는 Record Accumulator에 누적된 메시지 배치를 꺼내서 브로커에 전송함
* KafkaProducer의 Main Thread는 send() 메소드를 호출하고 Record Accumulator에 데이터 저장하고 Sender Thread는 별개로 데이터를 브로커로 전송

![](/images/kafka-03.png)

### Producer의 linger.ms와 batch.size

* Sender Thread는 기본적으로 전송할 준비가 되어 있으면 Record Accumulator에서 1개의 Batch를 가져갈 수도, 여러 개의 Batch를 가져갈 수도 있음. 또한 Batch에 미시지가 다 차지 않아도 가져 갈 수있음
* linger.ms를 0보다 크게 설정하여 Sender Thread가 하나의 Record Batch를 가져갈 때 일정 시간 대기하여 Record Batch에 메시지를 보다 많이 채울 수 있도록 적용

![](/images/kafka-04.png)

### Producer의 linger.ms에 대한 고찰

* linger.ms를 반드시 0보다 크게 설정할 필요는 없음
* Producer와 Broker간의 전송이 매우 빠르고 Producer에서 메시지를 적절한 Record Accumulator에누적된다면 linger.ms를 0으로 설정하여 빠르게 전송하는 것이 더 효율적일 수 있음
* 전반적인 Producer와 Broker간의 전송이 느리다면 linger.ms를 높여서 메시지가 배치로 적용될 수 있는 확율을 높이는 시도를 해볼 만함
* linger.ms는 보통 20ms 이하로 설정 권장

## Producer의 동기(Sync)와 비동기(Async)에서 배치 전송 차이

* 기본적으로 KafkaProducer 객체의 send() 메소드는 비동기이며 Batch 기반으로 메시지 전송
* Callback 기반의 Async는 비동기적으로 메시지를 보내면서 RecordMetadata를 Client가 받을 수 있는 방식을 제공
* Callback 기반의 Async는 여러 개의 메시지가 Batch로 만들어짐
* `RecordMetaData recordMetadata = producer.send(record, callback).get();`와 같은 방식으로 개별 메세지 별로 응답을 받을 때까지 block이 되는 방식으로는 메시지 배치처리가 불가, 전송은 배치레벨이지만 배치에 메시지는 단 1개

## Producer의 전송/재전송 내부 메커니즘 및 재 전송 동작 관련 주요 파라미터의 이해

![](/images/kafka-05.png)

| 옵션                   | 설명                                                                                |
|----------------------|-----------------------------------------------------------------------------------|
| `max.block.ms`       | Send() 호출시 Record Accumulator에 입력하지 못하고 block되는 최대 시간,<br/> 초과시 Timeout Exception |
| `linger.ms`          | Sender Thread가 Record Accumulator에서 배치별로 가져가기 위한 최대 대기시간                          |
| `request.timeout.ms` | 전송에 걸리는 최대 시간 <br/> 전송 재 시도 대기시간 제외 초과시 retry를 하거나 Timeout Exception 발생           |
| `retry.backoff.ms`   | 전송 재 시도를 위한 대기시간                                                                  |
| `deliver.timeout.ms` | Producer 메시지(배치) 전송에 허용된 최대 시간, 초과시 Timeout Exception 발생                          |

* `deliver.timeout.ms` >= `linger.ms` + `request.timeout.ms` 이상으로 설정 해야함
* retries = 10, `retry.backoff.ms` = 30, `request.timeout.ms` = 10,000ms
* `retry.backoff.ms`는 재 전송 주기 시간을 설정
* retries = 10, `retry.backoff.ms` = 30, `request.timeout.ms` = 10,000ms 경우에는 `request.timeout.ms` 기다린후 재 전송을하기전 30ms 이후 재전송 시도, 이와 같은 방식으로 재 전송을 10회 retry 해보고 더이상 retry 시도 하지 안흥ㅁ
* 만약 10회 이내에 `request.timeout.ms`에 도달하면 더 이상 retry 하지 않음

## Producer의 max.in.flight.requests.per.connection 파라미터와 배치 메시지의 전송순서 이해

### max.in.flight.requests.per.connection 이해

* 브로커 서버의 응답없이 Producerdml sender thread가 한번에 보낼 수 있는 메시지 배치의 개수, default 값은 5 Kafka Producer의 메시지 전송단위는 Batch임
* 비동기 전송 시 브로커의 응답없이 한꺼번에 보낼 수 있는 Batch의 개수는 max.in.flight.requests.per.connection 값에 의해 결정

![](/images/kafka-04.png)

### Producer 메시지 전송 순서와 Broker 메시지 저장 순서 고찰

![](/images/kafka-06.png)

* B0가 B1보다 먼저 Produce에서 생성된 메시지 배치
* max.in.flight.requests.per.connection = 2(>1) 에서 B0, B1 2개의 배치 메시지를 전송 시 B1은 성공저긍로 기록되었으나 B0의 경우 Write되지 않고 Ack 전송이 되지 않는 Failure 상황이 된 경우 Producer는 B0를 재전송하여 성공적으로 기록되며 Producer의 원래 메시지 순서와 다르게 Broker에 저장될 수 있음

## 최대 한번전송, 적어도 한번전송, 정확히 한번전송 이해


| **전송 순서 유형 (Delivery Mode)**   | **특징**                                          | **장점**                         | **단점**                   | **사용 사례**                         |
|--------------------------------|-------------------------------------------------|--------------------------------|--------------------------|-----------------------------------|
| **최대 한 번 전송 (At Most Once)**   | - 메시지가 한 번만 전송되거나, 전송되지 않을 수 있음<br> - 중복 메시지 없음 | - 중복 방지<br> - 빠른 전송            | - 메시지 손실 가능성             | 로깅, 트랜잭션 로그 등 손실이 허용 가능한 작업       |
| **적어도 한 번 전송 (At Least Once)** | - 메시지가 적어도 한 번 이상 전송됨<br> - 메시지가 중복될 수 있음       | - 메시지 손실 없음<br> - 데이터 완전성 보장   | - 중복 메시지 처리 필요           | 알림 시스템, 결제 트랜잭션 등 데이터 손실이 치명적인 경우 |
| **정확히 한 번 전송 (Exactly Once)**  | - 메시지가 중복 없이 정확히 한 번만 전송됨                       | - 데이터 완전성 + 중복 방지<br> - 안정적 처리 | - 높은 복잡도<br> - 성능 저하 가능성 | 주문 처리, 결제 시스템 등 정확성이 중요한 작업       |

위 표는 전송 순서 유형의 영문 명칭을 추가하여 한글과 함께 직관적으로 이해할 수 있도록 구성되었습니다.

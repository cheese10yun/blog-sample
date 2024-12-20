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

### 최대 한번 전송 (at most once)

```mermaid
sequenceDiagram
    participant Producer
    participant Broker
    Producer ->> Broker: 1. 메시지 A 전송
    Broker -->> Producer: 2. ACK 메시지 A
    Producer ->> Broker: 3. 메시지 B 전송
    Producer -x Broker: 4. ACK 또는 Error 응답 실패 (네트워크 장애 등)
    Producer ->> Broker: 5. 메시지 C 전송

```

- **acks = 0**: Producer가 브로커로부터 ACK나 에러 메시지 없이 다음 메시지를 연속적으로 보냅니다.
- 메시지가 정상적으로 브로커에 기록되더라도 Producer는 이를 확인하지 않습니다. 따라서 메시지가 손실될 수 있지만 중복 전송은 일어나지 않습니다.

1. **메시지 A 전송**: Producer가 메시지 A를 브로커로 전송하고, 브로커는 메시지를 정상적으로 기록하고 ACK를 전송합니다.
2. **ACK**: Producer는 ACK를 기다릴 필요 없이 다음 메시지를 계속 전송합니다.
3. **메시지 B 전송**: 메시지 B가 브로커에 정상적으로 기록되지 못하거나 네트워크 장애 등으로 ACK가 전송되지 않을 수 있습니다.
4. **메시지 C 전송**: 메시지 B의 상태를 확인하지 않고, 메시지 C를 전송합니다. 이로 인해 **메시지 B는 손실될 수 있습니다**.

### 적어도 한 번 전송 (At Least Once)

```mermaid
sequenceDiagram
    participant Producer
    participant Broker
    Producer ->> Broker: 1. 메시지 A 전송
    Broker -->> Producer: 2. ACK 메시지 A
    Producer ->> Broker: 3. 메시지 B 전송
    Producer -x Broker: 4. ACK 응답 실패 (네트워크 장애 등)
    Producer ->> Broker: 5. 메시지 B 재전송
    Broker -->> Producer: 6. ACK 메시지 B
    Producer ->> Broker: 7. 메시지 C 전송
```

- **acks = all (or 1)**: Producer는 브로커로부터 메시지에 대한 ACK를 반드시 받아야만 다음 메시지를 보냅니다.
- 네트워크 장애나 브로커 장애가 발생하여 ACK를 받지 못하면 Producer는 해당 메시지를 재전송합니다.
- 이 과정에서 메시지가 중복되어 브로커에 기록될 수 있습니다.

1. **메시지 A 전송**: Producer가 메시지 A를 브로커로 전송하고, 브로커는 정상적으로 메시지를 기록하고 ACK를 전송합니다.
2. **ACK**: Producer는 ACK를 받아 다음 메시지를 보낼 준비를 합니다.
3. **메시지 B 전송**: 메시지 B가 전송되었지만 네트워크 장애로 인해 ACK를 받지 못할 수 있습니다.
4. **메시지 B 재전송**: ACK를 받지 못했기 때문에 Producer는 메시지 B를 다시 전송합니다.
5. **중복 가능성**: 이로 인해 브로커에 메시지 B가 중복으로 기록될 가능성이 있습니다.

### 정확히 한 번 전송 (Exactly Once)

```mermaid
sequenceDiagram
    participant Producer
    participant Broker
    Producer ->> Broker: 1. 메시지 A 전송
    Broker -->> Producer: 2. ACK 메시지 A
    Producer ->> Broker: 3. 메시지 B 전송 (트랜잭션 시작)
    Broker -->> Producer: 4. 메시지 B 기록 및 ACK (트랜잭션 커밋)
    Producer ->> Broker: 5. 메시지 C 전송 (트랜잭션 시작)
    Broker -->> Producer: 6. 메시지 C 기록 및 ACK
```

- **acks = all**와 **idempotent 설정**: Producer와 Broker는 메시지가 정확히 한 번만 처리되도록 보장합니다.
- 트랜잭션을 활용해 메시지 전송과 기록이 원자적으로 이루어집니다.
- 네트워크 장애나 시스템 장애가 발생해도 중복 메시지가 기록되지 않도록 브로커가 상태를 관리합니다.

1. **메시지 A 전송**: Producer가 메시지 A를 브로커로 전송하고, 브로커는 ACK를 전송합니다.
2. **메시지 B 전송 (트랜잭션 시작)**: 메시지 B가 브로커에 전송되고, 이때 트랜잭션이 시작됩니다.
3. **트랜잭션 커밋**: 브로커는 메시지를 기록하고, Producer에게 ACK를 전송합니다. 메시지는 정확히 한 번만 기록됩니다.
4. **메시지 C 전송**: 이후 메시지 C도 동일한 방식으로 전송 및 기록됩니다.

## 커스텀 파티셔너(Custom Partitioner) 구현하기 - 01

### Producer의 메시지 파티셔닝

* KafkaProducer는 기본적으로 DefaultPartitioner 클래스를 이용하여 메시지 전송 시 도착할 Partition을 지정
* DefaultPartitioner는 키를 가지는 메시지의 경우 키 값을 Hasing하여 키 값에 따라 파티션 별로 균일하게 전송

### Producer의 메시지 Key값에 기반한 Custom 파티셔닝

## 커스텀 파티셔너(Custom Partitioner) 구현하기 - 02

## 커스텀 파티셔너(Custom Partitioner) 구현하기 - 03
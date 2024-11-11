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
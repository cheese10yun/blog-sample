import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class FirstAppProducer {

    private static String topicName = "first-app";

    public static void main(String[] args) {
        // (1) KafkaProducer에 필요한 설정
        final Properties conf = new Properties();

        conf.setProperty("bootstrap.servers", "kafka-broker01:9092,kafka-broker02:9092,kafka-broker03:9092");
        conf.setProperty("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        conf.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // (2) 카프카 클러스터에서 메시지를 송신(Produce) 하는 객체 생성
        final Producer<Integer, String> producer = new KafkaProducer<>(conf);

        int key;
        String value;

        for (int i = 1; i <= 100; i++) {
            key = i;
            value = String.valueOf(i);

            // (3) 송신 메시지 생성
            final ProducerRecord<Integer, String> record = new ProducerRecord<>(topicName, key, value);

            // (4) 메시지를 송신하고 Ack를 받을 때 실행할 작업 (Callback) 등록
            producer.send(record, (metadata, e) -> {
                if (metadata != null) {
                    // 송신에 성공한 경우의 처리
                    String infoString = String
                        .format("Success partition:%d, offset:%d", metadata.partition(), metadata.offset());
                    System.out.println(infoString);
                } else {
                    // 송신에 실패한 경우의 처리
                    String infoString = String.format("Failed:%s", e.getMessage());
                    System.err.println(infoString);
                }
            });
        }
        // (5) KafkaProducer를 클로즈하여 종료
        producer.close();
    }
}

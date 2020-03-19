import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public class FirstAppConsumer {

    private static String topicName = "first-app";

    public static void main(String[] args) {

        // (1) KafkaConsumer에 필요한 설정
        final Properties conf = new Properties();
        conf.setProperty("bootstrap.servers", "kafka-broker01:9092,kafka-broker02:9092,kafka-broker03:9092");
        conf.setProperty("group.id", "FirstAppConsumerGroup");
        conf.setProperty("enable.auto.commit", "false");
        conf.setProperty("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        conf.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // (2) Kafka클러스터에서 Message를 수신(Consume)하는 객체를 생성
        final Consumer<Integer, String> consumer = new KafkaConsumer<>(conf);

        // (3) 수신(subscribe)하는 Topic을 등록
        consumer.subscribe(Collections.singletonList(topicName));

        for (int count = 0; count < 300; count++) {
            // (4) Message를 수신하여, 콘솔에 표시한다
            ConsumerRecords<Integer, String> records = consumer.poll(1);
            for (ConsumerRecord<Integer, String> record : records) {
                String message = String.format("key:%d, value:%s", record.key(), record.value());
                System.out.println(message);

                // (5) 처리가 완료한 Message의 Offset을 Commit한다
                TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                OffsetAndMetadata oam = new OffsetAndMetadata(record.offset() + 1);
                Map<TopicPartition, OffsetAndMetadata> commitInfo = Collections.singletonMap(tp, oam);
                consumer.commitSync(commitInfo);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        // (6) KafkaConsumer를 클로즈하여 종료
        consumer.close();
    }
}
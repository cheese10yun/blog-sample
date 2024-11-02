package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.logging.Logger;

public class ProducerASyncWithKey {

    private static final Logger logger = Logger.getLogger(ProducerASyncWithKey.class.getName());

    public static void main(String[] args) {
        final String topicName = "multipart-topic";
        final Properties props = new Properties();

        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "121.168.58.79:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

        for (int seq = 0; seq < 20; seq++) {

            final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, String.valueOf(seq), "hello world " + seq);

            logger.info("seq: " +seq);

            kafkaProducer.send(producerRecord, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("\n##### record metadata received #### \n" +
                            "partition: " + metadata.partition() + "\n" +
                            "offset: " + metadata.offset() + "\n" +
                            "timestamp: " + metadata.timestamp() + "\n"
                    );
                } else {
                    logger.info("exception error from broker : " + exception.getMessage());
                }
            });
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        kafkaProducer.close();
    }
}

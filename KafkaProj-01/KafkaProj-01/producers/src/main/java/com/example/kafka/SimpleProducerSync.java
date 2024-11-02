package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class SimpleProducerSync {

    private static final Logger logger = Logger.getLogger(SimpleProducerSync.class.getName());

    public static void main(String[] args) {

        final String topicName = "simple-topic";

        final Properties props = new Properties();


        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.99:9092");

        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, "hello world2");


        try {
            final RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
            logger.info("\n##### record metadata received #### \n" +
                    "partition: " + recordMetadata.partition() + "\n" +
                    "offset: " + recordMetadata.offset() + "\n" +
                    "timestamp: " + recordMetadata.timestamp() + "\n"
            );
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            kafkaProducer.close();
        }

    }
}

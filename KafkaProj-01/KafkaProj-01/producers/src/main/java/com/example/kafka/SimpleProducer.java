package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class SimpleProducer {

    public static void main(String[] args) {

        final String topicName = "simple-topic";

        final Properties props = new Properties();


        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.99:9092");

        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, "hello world");

        kafkaProducer.send(producerRecord);

        kafkaProducer.flush();
        kafkaProducer.close();
    }
}

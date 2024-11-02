package com.example.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.logging.Logger;

public class ProducerASyncCustomCB {

    private static final Logger logger = Logger.getLogger(ProducerASyncCustomCB.class.getName());

    public static void main(String[] args) {
        final String topicName = "multipart-topic";
        final Properties props = new Properties();

        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "121.168.58.79:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<>(props);

        for (int seq = 0; seq < 20; seq++) {

            final ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(topicName, seq, "hello world " + seq);

            final Callback callback = new CustomCallback(seq);

            kafkaProducer.send(producerRecord, callback);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        kafkaProducer.close();
    }
}

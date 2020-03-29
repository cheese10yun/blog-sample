package com.example.kafkasamplenew


import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@Configuration
@EnableConfigurationProperties(KafkaProperties::class)
class KafkaProducerConfig(
    private val properties: KafkaProperties
) {

    @Bean
    fun producerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "192.168.0.10:9092"
        props[ProducerConfig.RETRIES_CONFIG] = 0
        props[ProducerConfig.BATCH_SIZE_CONFIG] = 4096
        props[ProducerConfig.LINGER_MS_CONFIG] = 1
        props[ProducerConfig.BUFFER_MEMORY_CONFIG] = 40960
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return props
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }


    fun producerFactory(): ProducerFactory<String, String> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

}
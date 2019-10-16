package com.example.kafkasample

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.util.*

@Configuration
@EnableKafka
@PropertySource("classpath:kafka.properties")
class KafkaConfiguration(private val env: Environment) {


    @Bean
    open fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(DefaultKafkaProducerFactory(producerConfig()))
    }

    private fun producerConfig(): HashMap<String, Any?> {
        val config = HashMap<String, Any?>()

        config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = env.getProperty("bootstrap.yml")
        config[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        config[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java

        return config
    }

}

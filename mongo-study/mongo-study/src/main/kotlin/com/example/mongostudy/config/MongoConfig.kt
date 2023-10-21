package com.example.mongostudy.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.EnableMongoAuditing

const val MONGO_TRANSACTION_MANAGER = "mongoTransactionManager"

@Configuration
@EnableMongoAuditing
class MongoConfig {

    @Bean(MONGO_TRANSACTION_MANAGER)
    fun mongoTransactionManager(factory: MongoDatabaseFactory) =
        MongoTransactionManager(factory)

}
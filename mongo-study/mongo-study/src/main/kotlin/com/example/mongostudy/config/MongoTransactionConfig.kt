package com.example.mongostudy.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

const val MONGO_TRANSACTION_MANAGER = "mongoTransactionManager"

@Configuration
@EnableMongoAuditing
class MongoTransactionConfig {

    @Bean(MONGO_TRANSACTION_MANAGER)
    fun mongoTransactionManager(factory: MongoDatabaseFactory) =
        MongoTransactionManager(factory)

}

@Configuration
class MongoConfiguration {

    @Bean
    fun mongoTemplate(
        mongoDbFactory: MongoDatabaseFactory,
        mongoConverter: MappingMongoConverter
    ): MongoTemplate {
        return MongoTemplate(mongoDbFactory, mongoConverter)
    }

//    @Bean
//    fun mappingMongoConverter(
//        mongoDbFactory: MongoDatabaseFactory,
//        mongoMappingContext: MongoMappingContext
//    ): MappingMongoConverter {
//        val dbRefResolver = DefaultDbRefResolver(mongoDbFactory)
//        val mappingMongoConverter = MappingMongoConverter(dbRefResolver, mongoMappingContext)
//
////        // _class 필드를 포함하지 않도록 설정
////        mappingMongoConverter.setTypeMapper(DefaultMongoTypeMapper(null))
//        return mappingMongoConverter
//    }

    // 필요한 다른 빈 설정들...
}
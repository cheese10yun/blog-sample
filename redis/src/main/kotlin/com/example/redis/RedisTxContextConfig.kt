package com.example.redis

import java.sql.SQLException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement


//@Configuration
//@EnableTransactionManagement
//class RedisTxContextConfig {
//
////    @Bean
////    fun transactionManager(): PlatformTransactionManager {
////        return DataSourceTransactionManager(dataSource())
////    }
//
//}
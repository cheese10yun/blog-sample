//package com.batch.payment.domain.config
//
//import org.jetbrains.exposed.sql.Database
//import org.springframework.context.annotation.Bean
//import org.springframework.stereotype.Component
//import javax.sql.DataSource
//
//@Component
//class ExposedConfig(
//    private val dataSource: DataSource
//) {
//
//    @Bean
//    fun exposedDataBase() =
//        Database.connect(dataSource)
//}
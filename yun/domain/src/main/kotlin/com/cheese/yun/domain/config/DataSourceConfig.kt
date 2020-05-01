package com.cheese.yun.domain.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Bean
    fun dataSource(
        properties: DataSourceProperties
    ): DataSource {
        return DataSourceBuilder.create()
            .password(properties.password)
            .username(properties.username)
            .url(properties.jdbcUrl)
            .type(HikariDataSource::class.java)
            .driverClassName(properties.driverClassName)
            .build()
    }

    @Component
    @ConfigurationProperties("datasource")
    @PropertySources(
        PropertySource(value = ["classpath:/datasource/datasource-\${spring.profiles.active}.properties"])
    )
    class DataSourceProperties : HikariConfig()
}

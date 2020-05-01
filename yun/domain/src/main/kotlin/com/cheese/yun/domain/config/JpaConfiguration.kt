package com.cheese.yun.domain.config

import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

const val basePackages = "com.cheese.yun.domain"

@Configuration
@EnableJpaRepositories(
    basePackages = [basePackages],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
class JpaConfiguration {

    @Bean
    fun entityManagerFactory(
        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
        dataSource: DataSource,
        jpaProperties: JpaProperties,
        hibernateProperties: HibernateProperties
    ): LocalContainerEntityManagerFactoryBean {
        return entityManagerFactoryBuilder.dataSource(dataSource)
            .properties(
                hibernateProperties.determineHibernateProperties(
                    jpaProperties.properties, HibernateSettings()
                )
            )
            .packages(basePackages)
            .build()
    }

    @Bean
    fun transactionManager(
        entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager(entityManagerFactory)

}
package com.example.exposedstudy

import javax.persistence.EntityManagerFactory
import javax.sql.DataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager

@SpringBootApplication
class ExposedStudyApplication

fun main(args: Array<String>) {
    runApplication<ExposedStudyApplication>(*args)
}

const val PROPERTIES = "spring.datasource"


const val basePackages = "com.example.exposedstudy"

//@Configuration
@Configuration
@EnableJpaRepositories(
        basePackages = [basePackages],
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
class JpaConfiguration {
    @Bean
    fun entityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            dataSource: DataSource,
            jpaProperties: JpaProperties,
            hibernateProperties: HibernateProperties
    ) = builder.dataSource(dataSource)
            .properties(hibernateProperties.determineHibernateProperties(
                    jpaProperties.properties, HibernateSettings()))
            .packages(basePackages)
            .build()

    @Bean
    fun transactionManager(
            entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager(entityManagerFactory)

}
//package com.example.springtransaction
//
//import javax.persistence.EntityManagerFactory
//import javax.sql.DataSource
//import org.springframework.beans.factory.annotation.Qualifier
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories
//import org.springframework.orm.jpa.JpaTransactionManager
//
//const val BASE_PACKAGES = "com.example.springtransaction"
//const val MASTER_ENTITY_MANAGER_FACTORY = "entityManagerFactory"
//const val SLAVE_ENTITY_MANAGER_FACTORY = "slaveEntityManagerFactory"
//const val MASTER_TX_MANAGER = "transactionManager"
//
//@Configuration
//@EnableJpaRepositories(
//    basePackages = [BASE_PACKAGES],
//    entityManagerFactoryRef = MASTER_ENTITY_MANAGER_FACTORY,
//    transactionManagerRef = MASTER_TX_MANAGER
//)
//class EntityManagerConfiguration {
//
//    @Primary
//    @Bean(name = [MASTER_ENTITY_MANAGER_FACTORY])
//    fun entityManagerFactory(
//        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
//        @Qualifier(MASTER_DATASOURCE) dataSource: DataSource,
//        jpaProperties: JpaProperties,
//        hibernateProperties: HibernateProperties
//    ) = entityManagerFactoryBuilder
//        .dataSource(dataSource)
//        .properties(
//            hibernateProperties.determineHibernateProperties(
//                jpaProperties.properties,
//                HibernateSettings()
//            )
//        )
//        .packages(BASE_PACKAGES)
//        .persistenceUnit("master")
//        .build()
//
//    @Bean(name = [SLAVE_ENTITY_MANAGER_FACTORY])
//    fun slaveEntityManagerFactory(
//        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
//        @Qualifier(SLAVE_DATASOURCE) dataSource: DataSource,
//        jpaProperties: JpaProperties,
//        hibernateProperties: HibernateProperties
//    ) = entityManagerFactoryBuilder
//        .dataSource(dataSource)
//        .properties(
//            hibernateProperties.determineHibernateProperties(
//                jpaProperties.properties,
//                HibernateSettings()
//            )
//        )
//        .packages(BASE_PACKAGES)
//        .persistenceUnit("slavea")
//        .build()
//
//    @Primary
//    @Bean(name = [MASTER_TX_MANAGER])
//    fun transactionManager(entityManagerFactory: EntityManagerFactory) =
//        JpaTransactionManager(entityManagerFactory)
//
//}
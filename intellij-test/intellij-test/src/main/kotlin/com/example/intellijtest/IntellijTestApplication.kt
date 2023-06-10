package com.example.intellijtest

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class IntellijTestApplication

fun main(args: Array<String>) {
    runApplication<IntellijTestApplication>(*args)
}


@Configuration
class AppRunner(
    private val shopRepository: ShopRepository,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {

        (1..5).map {
            Shop(
                brn = "brn$-{it}",
                name = "name$-{it}",
                band = "band$-{it}",
                category = "category$-{it}",
                email = "email$-{it}",
                website = "website$-{it}",
                openingHours = "openingHours$-{it}",
                seatingCapacity = it,
                rating = it,
                address = "address$-{it}",
                addressDetail = "addressDetail$-{it}",
                zipCode = "zipCode$-{it}",
            )
        }.also {
            shopRepository.saveAll(it)
        }

    }
}

//@Configuration
//class Configuration {
//
//    @Bean
//    fun query(entityManager: EntityManager): JPAQueryFactory {
//        return JPAQueryFactory(entityManager)
//    }
//}


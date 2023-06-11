package com.example.intellijtest

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    private val memberRepository: MemberRepository
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

        (1..5).map {
            Member(
                firstName = "firstName",
                lastName = "lastName",
                email = "test-${it}@asd.com",
                phoneNumber = "phoneNumber",
                address = "address",
                age = it,
                gender = "gender",
                occupation = "occupation",
                residentRegistrationNumber = "residentRegistrationNumber",
                status = MemberStatus.UNVERIFIED
            )

        }.also {
//            it.first().age = 123

            memberRepository.saveAll(it)
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


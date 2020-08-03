package com.spring.cloud.organization

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class OrganizationServiceApplication

fun main(args: Array<String>) {
    runApplication<OrganizationServiceApplication>(*args)
}


@Component
class OrganizationServiceApplicationRunner(
    private val organizationRepository: OrganizationRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        organizationRepository.save(
            Organization(
                name = "name",
                contactPhone = "contactPhone",
                contactName = "contactName",
                contactEmail = "contactEmail"
            )
        )
    }
}
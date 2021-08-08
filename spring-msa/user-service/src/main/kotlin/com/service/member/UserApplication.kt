package com.service.member

import com.service.member.user.User
import com.service.member.user.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@RefreshScope
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}

@Component
class AppRunner(
    private val userRepository: UserRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        userRepository.saveAllAndFlush(
            listOf(
                User(
                    email = "asd@asd.cm",
                    name = "yun",
                    userId = "5566da6f-3f03-4ce5-8863-3c142e452522",
                    password = "\$2a\$10\$Inf2wE5nDnN/4pynduvud.h7sVm5TuNcvPt5m9r8ZpCoJCiAWjWzu"
                ),
                User(
                    email = "qwe@asd.cm",
                    name = "Kim",
                    userId = "997a5a8b-80e4-4a5d-b5d1-14ee22be18da",
                    password = "\$2a\$10\$Inf2wE5nDnN/4pynduvud.h7sVm5TuNcvPt5m9r8ZpCoJCiAWjWzu"
                )
            )
        )
    }

}
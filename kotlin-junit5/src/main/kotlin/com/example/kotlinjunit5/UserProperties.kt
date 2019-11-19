package com.example.kotlinjunit5

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@ConstructorBinding
@ConfigurationProperties(prefix = "user")
@Validated
data class UserProperties(
        @field:Email
        val email: String,

        @field:NotEmpty
        val nickname: String,

        @field:Min(10)
        val age: Int,

        @field:NotNull
        val auth: Boolean,

        @field:Min(10)
        val amount: Double,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @field:NotNull
        val date: LocalDate
)
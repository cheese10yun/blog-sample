package com.example.kotlinjunit5

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@ConstructorBinding
@ConfigurationProperties(prefix = "sample")
@Validated
data class SampleProperties(
        @field:Email
        val email: String,

        @field:Min(10)
        val number: Long,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @field:NotNull
        val date: LocalDate
)
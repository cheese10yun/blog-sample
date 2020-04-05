package com.example.api

import com.example.api.filter.HttpLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.Filter

class Config {
}

@Configuration
class FilterConfig {
    @Bean
    fun httpLoggingFilter(): Filter = HttpLoggingFilter()
}
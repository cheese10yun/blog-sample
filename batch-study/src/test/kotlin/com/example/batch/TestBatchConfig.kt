package com.example.batch

import com.example.batch.batch.config.QuerydslConfiguration
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


@Configuration
@EnableBatchProcessing
@Import(QuerydslConfiguration::class)
class TestBatchConfig
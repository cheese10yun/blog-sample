package com.cheese.yun.domain.config

import org.springframework.context.annotation.Import
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FILE

@Target(CLASS, FILE)
@Retention(RUNTIME)
@MustBeDocumented
@Import(JpaConfiguration::class, DataSourceConfig::class)
annotation class EnableDomain

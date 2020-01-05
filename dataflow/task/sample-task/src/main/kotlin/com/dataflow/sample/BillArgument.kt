package com.dataflow.sample

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConstructorBinding
@ConfigurationProperties("argument")
@Validated
data class BillArgument(
        val name: String
) {

}
package com.dataflow.sample

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated


@ConfigurationProperties("argument")
@Validated
data class BillArgument(
        val name: String
) {

}
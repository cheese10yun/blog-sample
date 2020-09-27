package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject

class FuelClient(
    private val host: String = "http://localhost:8080",
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .apply { this.propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE }
) {

    fun getSample(): SampleResponse = "$host/sample"
        .httpGet()
        .response()
        .first.responseObject<SampleResponse>(objectMapper)
        .third.get()
}

data class SampleResponse(
    val foo: String,
    val bar: String
)
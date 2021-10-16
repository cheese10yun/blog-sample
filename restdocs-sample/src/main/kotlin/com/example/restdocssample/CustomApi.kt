package com.example.restdocssample

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/custom")
class CustomApi {

    @PostMapping
    fun custom(@RequestBody dto: CustomRequest) {

    }


    data class CustomRequest(
        val name: String,
        val email: String
    )
}
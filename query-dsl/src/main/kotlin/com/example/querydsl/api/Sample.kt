package com.example.querydsl.api

import com.example.querydsl.service.ApiService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sample")
class Sample(
    private val apiService: ApiService
) {

    @PostMapping
    fun aa() {

    }


}
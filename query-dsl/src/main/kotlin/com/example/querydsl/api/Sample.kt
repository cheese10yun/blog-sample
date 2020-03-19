package com.example.querydsl.api

import com.example.querydsl.service.ApiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sample")
class Sample(
    private val apiService: ApiService
) {




}
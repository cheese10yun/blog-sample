package com.example.eurekaclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient("sampleService")
interface SampleFeignClient {

    @GetMapping("/sample/{id}")
    fun sample(@PathVariable("id") id: String)

}
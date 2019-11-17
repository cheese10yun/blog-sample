package com.example.hystrix

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class Config {

    @LoadBalanced
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @HystrixCommand(
            commandProperties = [HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10")]
    )
    fun getOrganization(organizationId: String) {

    }

//    @HystrixCommand(fallbackMethod = "buildFallbackLicenseList")
//    fun getLicenses(organizationId: String): List<String> {
//        return getLicenses();
//    }
//
//    private fun buildFallbackLicenseList(organizationId: String) {
//        return listOf<>(License()
//                .withId("11232323")
//                .withProductName("sample"))
//    }

    @HystrixCommand(
            fallbackMethod = "buildFallbackLicenseList",
            threadPoolKey = "licenseByOrgThreadPool",
            threadPoolProperties = [
                HystrixProperty(name = "coreSize", value = "30"),
                HystrixProperty(name = "maxQueueSize", value = "30"),
            ]
    )

}
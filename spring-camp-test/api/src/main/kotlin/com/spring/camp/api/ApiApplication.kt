package com.spring.camp.api

import com.spring.camp.io.PartnerClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}

@Service
class PartnerObtainService(
    private val obtainService: ObtainService,
) {

    fun syncPartner() {
        obtainService.syncPartner()
    }

}

@Service
class ObtainService(
    private val partnerClient: PartnerClient,
) {

    fun syncPartner(): Boolean {
//        partnerClient.getPartnerStatus()
        return true
    }
}

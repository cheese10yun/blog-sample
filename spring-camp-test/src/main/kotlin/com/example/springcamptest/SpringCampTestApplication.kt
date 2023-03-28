package com.example.springcamptest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service

@SpringBootApplication
class SpringCampTestApplication

fun main(args: Array<String>) {
    runApplication<SpringCampTestApplication>(*args)
}

@Service
class PartnerObtainService(
    private val obtainService:ObtainService
) {

    fun syncPartner(){
        obtainService.syncPartner()
    }

}

@Service
class ObtainService(
    private val partnerClient: PartnerClient,
){

    fun syncPartner(): Boolean {
        partnerClient.syncPartner()
        return true

    }

}

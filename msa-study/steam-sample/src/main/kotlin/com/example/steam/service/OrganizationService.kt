package com.example.steam.service

import com.example.steam.event.SimpleSourceBean
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrganizationService(
        private val simpleSourceBean: SimpleSourceBean
) {

    fun saveOrg() {
        val orgId = UUID.randomUUID().toString()
        simpleSourceBean.publishOrgChange("save", orgId)
    }
}
package com.example.steam.event

import com.example.steam.model.OrganizationChangeModel
import org.springframework.cloud.stream.messaging.Source
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import kotlin.reflect.jvm.jvmName

@Component
class SimpleSourceBean(
        private val source: Source
) {

    fun publishOrgChange(action: String, orgId: String) {
        println("Sending Kafka message: $action, for Organization Id: $orgId")

        val model = OrganizationChangeModel(
                OrganizationChangeModel::class.jvmName,
                action, orgId
        )

        source
                .output()
                .send(MessageBuilder
                        .withPayload(model)
                        .build()
                )
    }
}
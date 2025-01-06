package com.example.mongostudy

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import org.bson.types.ObjectId

object DiffManager {

    private val diffMapper = jacksonObjectMapper().apply {
        registerModules(SimpleModule().apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            addSerializer(ObjectId::class.java, ObjectIdSerializer())
        })
    }

    fun <T, K, S> calculateDifferences(
        originItems: List<T>,
        newItems: List<T>,
        associateByKey: (T) -> K,
        groupByKey: (T) -> S
    ): Map<S, Map<String, DiffValue<String, String>>> {
        val originalAssociate = originItems.associateBy(associateByKey)
        val newAssociate = newItems.associateBy(associateByKey)
        val changes = newAssociate.flatMap { (id, newItem) ->
            val originalItem = originalAssociate[id]
            when {
                originalItem != null -> {
                    val originalNode = diffMapper.valueToTree<JsonNode>(originalItem)
                    val newNode = diffMapper.valueToTree<JsonNode>(newItem)
                    val diffNode = JsonDiff.asJson(originalNode, newNode)

                    when {
                        diffNode.size() > 0 -> {
                            diffNode.mapNotNull { node ->
                                val path = node.get("path").asText().removePrefix("/")
                                val originValue = originalNode.at("/$path").asText()
                                val newValue = newNode.at("/$path").asText()

                                Triple(
                                    first = groupByKey(newItem),
                                    second = path,
                                    third = DiffValue(origin = originValue, new = newValue)
                                )
                            }
                        }
                        else -> emptyList()
                    }
                }
                else -> emptyList()
            }
        }

        // 결과를 groupByKey로 그룹화하고, 각 그룹 내의 변경 사항을 Map으로 변환
        return changes
            .groupBy({ it.first }, { it.second to it.third })
            .mapValues { (_, value) -> value.toMap() }
    }
}

class ObjectIdSerializer : JsonSerializer<ObjectId>() {
    override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}


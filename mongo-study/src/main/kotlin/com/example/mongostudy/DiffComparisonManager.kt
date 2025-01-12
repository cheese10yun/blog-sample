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

typealias DiffValueTracker = Map<String, DiffValue<String, String>>
typealias DiffTriple = Triple<String, String, String>

object DiffComparisonManager {

    private val diffMapper = jacksonObjectMapper()
        .apply {
            registerModules(
                SimpleModule().apply {
                    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                    addSerializer(ObjectId::class.java, ObjectIdSerializer())
                }
            )
        }

    fun <T, K, S> calculateDifferences(
        originItems: List<T>,
        newItems: List<T>,
        associateByKey: (T) -> K,
        groupByKey: (T) -> S
    ): Map<S, DiffValueTracker> {
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
                                val (path, originValue, newValue) = extractDiffValue(node, originalNode, newNode)

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

    fun <T> calculateDifference(
        originItem: T,
        newItem: T
    ): DiffValueTracker {
        val originalNode = diffMapper.valueToTree<JsonNode>(originItem)
        val newNode = diffMapper.valueToTree<JsonNode>(newItem)
        val diff = JsonDiff.asJson(originalNode, newNode)
        return when {
            diff.size() > 0 -> {
                diff.mapNotNull { diffNode ->
                    val (path, originValue, newValue) = extractDiffValue(diffNode, originalNode, newNode)
                    Pair(
                        first = path,
                        second = DiffValue(originValue, newValue)
                    )
                }
                    .toMap()
            }
            else -> emptyMap()
        }
    }

    private fun extractDiffValue(node: JsonNode, originalNode: JsonNode, newNode: JsonNode): DiffTriple {
        val path = node.get("path").asText().removePrefix("/")
        val originValue = originalNode.at("/$path").asText()
        val newValue = newNode.at("/$path").asText()
        return DiffTriple(path, originValue, newValue)
    }

}

class ObjectIdSerializer : JsonSerializer<ObjectId>() {
    override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}


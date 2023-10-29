package com.example.mongostudy

import com.mongodb.assertions.Assertions.assertFalse
import java.util.UUID
import org.junit.jupiter.api.Test

class Generator {

    fun generateUniqueIdBasedOnTime(): Long {
        val currentTime = System.currentTimeMillis()
        var uniqueId = currentTime - (currentTime % 1_000_000_000_000L) + (currentTime % 1_000_000_000_000L)

        if (uniqueId in 1..100_000) {
            uniqueId += 100_001
        }

        return uniqueId
    }

    fun generateUniqueIdBasedOnUUID(): Long {
        var uniqueId = UUID.randomUUID().mostSignificantBits

        if (uniqueId < 0) {
            uniqueId = -uniqueId
        }

        if (uniqueId in 1..100_000) {
            uniqueId += 100_001
        }

        return uniqueId
    }
}



class GeneratorTest(){

    val generator = Generator()

//    @Test
//    fun `generateUniqueIdBasedOnTime should generate unique id`() {
//        val excludedRange = 1_500_000_000_000L..1_500_000_010_000L
//        val generatedIds = mutableSetOf<Long>()
//
//        for (i in 1..1000) {
//            val uniqueId = generator.generateUniqueIdBasedOnTime(excludedRange)
//            assertFalse(uniqueId in excludedRange)
//            assertFalse(uniqueId in generatedIds)
//            generatedIds.add(uniqueId)
//        }
//    }

    @Test
    fun `generateUniqueIdBasedOnUUID should generate unique id`() {
        val generatedIds = mutableSetOf<Long>()

        for (i in 1..1000) {
            val uniqueId = generator.generateUniqueIdBasedOnUUID()
            assertFalse(uniqueId in generatedIds)
            generatedIds.add(uniqueId)
        }

        println(generatedIds)
    }
}
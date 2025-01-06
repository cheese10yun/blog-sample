package com.example.mongostudy

import org.junit.jupiter.api.Test

class DiffInfoRepositoryTest(
    private val diffInfoRepository: DiffInfoRepository
) : MongoStudyApplicationTests() {


    @Test
    fun `calculateDifferences test`() {
        val diffInfo = diffInfoRepository.save(
            DiffInfo(
                key = "1",
                name = "name_1",
                email = "email_1",
            )
        )

        val copy = diffInfo.copy(
            key = "1",
            name = "name_2",
            email = "name_2"

        )
        val originItems = listOf(diffInfo)
        val newItems = listOf(copy)
        val calculateDifferences = DiffManager.calculateDifferences(
            originItems = originItems,
            newItems = newItems,
            associateByKey = DiffInfo::key,
            groupByKey = DiffInfo::key
        )

        diffInfo.diff = calculateDifferences[diffInfo.key]!!
        diffInfoRepository.save(diffInfo)
    }
}
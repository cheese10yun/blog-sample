package com.example.mongostudy

import org.junit.jupiter.api.Test

class DiffInfoRepositoryTest(
    private val diffInfoRepository: DiffInfoRepository
) : MongoStudyApplicationTests() {

    @Test
    fun `calculateDifferences test`() {
        val originDiffInfo = diffInfoRepository.save(
            DiffInfo(
                key = "1",
                name = "name_1",
                email = "email_1",
            )
        )

        val newDiffInfo = originDiffInfo.copy(
            key = "1",
            name = "name_2",
            email = "name_2"

        )
        val originItems = listOf(originDiffInfo)
        val newItems = listOf(newDiffInfo)
        val calculateDifferences = DiffManager.calculateDifferences(
            originItems = originItems,
            newItems = newItems,
            associateByKey = DiffInfo::key,
            groupByKey = DiffInfo::key
        )

        originDiffInfo.diff = calculateDifferences[originDiffInfo.key]!!
        diffInfoRepository.save(originDiffInfo)
    }
}
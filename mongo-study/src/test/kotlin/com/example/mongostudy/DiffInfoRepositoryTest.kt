package com.example.mongostudy

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class DiffInfoRepositoryTest(
    private val diffInfoRepository: DiffInfoRepository
) : MongoStudyApplicationTests() {

    @Test
    fun `calculateDifferences test`() {
        // given
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

        // when
        val calculateDifferences = DiffManager.calculateDifferences(
            originItems = originItems,
            newItems = newItems,
            associateByKey = DiffInfo::key,
            groupByKey = DiffInfo::key
        )
        // then
        originDiffInfo.diff = calculateDifferences[originDiffInfo.key]!!
        val save = diffInfoRepository.save(originDiffInfo)

        then(save.diff).isEqualTo(calculateDifferences[originDiffInfo.key]!!)
    }
}
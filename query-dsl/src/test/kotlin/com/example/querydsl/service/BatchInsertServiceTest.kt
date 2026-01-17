package com.example.querydsl.service

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Writer
import com.example.querydsl.domain.WriterCustomRepository
import com.example.querydsl.domain.WriterRepository
import com.example.querydsl.domain.WriterType
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch
import org.assertj.core.api.Assertions.assertThat


//@Transactional
class BatchInsertServiceTest(
    private val batchInsertService: BatchInsertService,
    private val writerRepository: WriterRepository,
    ) : SpringBootTestSupport() {
    /**
     *
     * rows	saveAll (B)	add batch (C)	성능 개선율 (D)
     * 100	104	12	88.50%
     * 200	174.5	16	90.80%
     * 500	370.25	27.5	92.60%
     * 1,000	695	56	91.90%
     * 2,000	1,574	68	95.70%
     * 5,000	3,778	140	96.30%
     * 10,000	7,505	265	96.50%
     *
     */
    @Test
    fun `saveAll test`() {
        val rowsList = listOf(
            100,
            200,
            500,
            1_000,
            2_000,
            5_000,
            10_000,
        )

        val iterations = 5
        rowsList.forEach { rows ->
            var totalTimeMillis = 0.0
            println("--- saveAll test 성능 측정 시작 (${iterations}회 반복, 첫 회 제외) ---")
            for (i in 1..iterations) {
                // 각 반복마다 새로운 writer 리스트를 생성하여 동일한 데이터로 인한 PK 충돌 방지
                val uniqueWriters = (1..rows).map {
                    Writer(
                        name = "name-$i-$it",
                        email = "email-$i-$it",
                        score = it,
                        reputation = 0.0,
                        active = false,
                        )
                }

                val stopWatch = StopWatch()
                stopWatch.start()
                writerRepository.saveAll(uniqueWriters)

                stopWatch.stop()
                val executionTime = stopWatch.totalTimeMillis

                if (i == 1) {
                    println("첫 회차 실행 시간 (평균에서 제외): ${executionTime} ms")
                } else {
                    totalTimeMillis += executionTime
                    println("$i 회차 실행 시간: ${executionTime} ms")
                }
            }

            val effectiveIterations = if (iterations > 1) iterations - 1 else 1 // 첫 회 제외
            val averageTimeMillis = if (effectiveIterations > 0) totalTimeMillis / effectiveIterations else 0.0

            println("--- 측정 완료 ---")
            println("첫 회를 제외한 $rows 평균 실행 시간: ${averageTimeMillis} ms")
        }
    }

    @Test
    fun `executeBulkInsertWritersWithSql test`() {
        val rowsList = listOf(
            100,
            200,
            500,
            1_000,
            2_000,
            5_000,
            10_000,
        )

        val iterations = 5
        rowsList.forEach { rows ->
            var totalTimeMillis = 0.0
            println("--- executeBulkInsertWritersWithSql 성능 측정 시작 (${iterations}회 반복, 첫 회 제외) ---")
            for (i in 1..iterations) {
                // 각 반복마다 새로운 writer 리스트를 생성하여 동일한 데이터로 인한 PK 충돌 방지
                val uniqueWriters = (1..rows).map {
                    Writer(
                        name = "name-$i-$it",
                        email = "email-$i-$it"
                    )
                }

                val stopWatch = StopWatch()
                stopWatch.start()
                batchInsertService.executeBulkInsertWritersWithSql(uniqueWriters)
                stopWatch.stop()
                val executionTime = stopWatch.totalTimeMillis

                if (i == 1) {
                    println("첫 회차 실행 시간 (평균에서 제외): ${executionTime} ms")
                } else {
                    totalTimeMillis += executionTime
                    println("$i 회차 실행 시간: ${executionTime} ms")
                }
            }

            val effectiveIterations = if (iterations > 1) iterations - 1 else 1 // 첫 회 제외
            val averageTimeMillis = if (effectiveIterations > 0) totalTimeMillis / effectiveIterations else 0.0

            println("--- 측정 완료 ---")
            println("첫 회를 제외한 $rows 평균 실행 시간: ${averageTimeMillis} ms")
        }
    }
}

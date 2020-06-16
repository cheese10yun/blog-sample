package com.example.querydsl

import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SampleTest {

    @BeforeAll
    internal fun beforeAll() {
        println("BeforeAll : 테스트 실행되기 이전 단 한 번만 실행")
    }

    @AfterAll
    internal fun afterAll() {
        println("AfterAll : 테스트 실행 이후 단 한 번 실행됨")
    }

    @BeforeEach
    internal fun beforeEach() {
        println("BeforeEach : 모든 테스트 마다 실행되기 이전 실행")
    }

    @AfterEach
    internal fun afterEach() {
        println("AfterEach : 모든 테스트 마다 실행 이후 실행")
    }

    @Test
    internal fun `test code1`() {
        println("test code run 1")
    }

    @Test
    internal fun `test code2`() {
        println("test code run 2")
    }
}
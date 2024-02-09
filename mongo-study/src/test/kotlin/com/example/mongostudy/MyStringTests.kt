package com.example.mongostudy

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe


class MyStringTests : StringSpec({
    "대문자 변환 테스트" {
        // 테스트 대상 문자열
        val original = "hello world"
        // 대문자로 변환
        val uppercased = original.uppercase()

        // 검증
        uppercased shouldBe "HELLO WORLD"
    }
})


class CalculatorSpec : BehaviorSpec({
    // 계산기 클래스
    class Calculator {
        fun add(a: Int, b: Int): Int = a + b
    }

    val calculator = Calculator()

    Given("계산기가 주어졌을 때") {
        When("5와 3을 더하면") {
            val result = calculator.add(5, 3)
            Then("결과는 8이어야 한다") {
                result shouldBe 8
            }
        }
        When("10과 -2를 더하면") {
            val result = calculator.add(10, -2)
            Then("결과는 8이어야 한다") {
                result shouldBe 8
            }
        }
    }
})

class AnnotationTest : AnnotationSpec() { // 어노테이션 형태로 작성, JUnit과 비슷하다.

    /**
     * int형 파라미터 2개를 받아서 합계를 내는 함수를 만들어줘요.
     */
    fun sum(a: Int, b: Int): Int {
        return a + b
    }

    @BeforeEach
    fun beforeTest() {
        println("Before each test")
    }

    @Test
    fun test1() {
        1 shouldBe 1
    }

    @Test
    fun test2() {
        3 shouldBe 3
    }
}
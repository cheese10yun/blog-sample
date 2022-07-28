package com.kotlin.cookbook.step2

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import kotlin.math.pow

class PersonTest {

    @Test
    fun name() {
        val person = Person(
            first = "first",
            middle = null,
            last = "last"
        )

        val middleLength = person.middle?.length ?: 0
    }

    @Test
    fun `코틀린에서 중복 함수 변경 호출하기`() {
        // default 메서드에 따라 3개 메서드 구현
        val person = Person(
            first = "first",
            middle = null,
            last = "last"
        )
        person.addProduct(name = "name", price = 10, "DESC")
        person.addProduct(name = "name", price = 10)
        person.addProduct(name = "name")
    }

    @Test
    fun `2진수 표현`() {
        val toString = 42.toString(2)
        println(toString)
    }

    @Test
    fun `정수를 지수로 만들기`() {
        val toInt = 2.toDouble().pow(8).toInt()
        println(toInt)
    }

    @Test
    fun `2를 곱하거나 나누기`() {
        // shl
        then(2).isEqualTo(1 shl 1)
        then(4).isEqualTo(1 shl 2)
        then(8).isEqualTo(1 shl 3)
        then(16).isEqualTo(1 shl 4)
        then(32).isEqualTo(1 shl 5)
        then(64).isEqualTo(1 shl 6)
        then(128).isEqualTo(1 shl 7)

        // shr
        then(117).isEqualTo(235 shr 1)
        then(58).isEqualTo(235 shr 2)
        then(29).isEqualTo(235 shr 3)
        then(14).isEqualTo(235 shr 4)
        then(7).isEqualTo(235 shr 5)
        then(3).isEqualTo(235 shr 6)
    }

    @Test
    fun `숫자 4비트 반전`() {
        // 4 == 0b0000_0100
        // 주어진 비트 보수
        then(-5).isEqualTo(4.inv())
    }

    @Test
    fun `and or xor 간단한 예`() {
        val n1 = 0b000_1100 // 십진수 12
        val n2 = 0b001_1001 // 십진수 25

        val n1_and_n2 = n1 and n2
        val n1_or_n2 = n1 or n2
        val n1_xor_n2 = n1 xor n2

        then(n1_and_n2).isEqualTo(0b000_1000) // 8
        then(n1_or_n2).isEqualTo(0b001_1101) // 29
        then(n1_xor_n2).isEqualTo(0b001_0101) // 21
    }

    @Test
    fun `mapOf 인자인 pair를 생성하기 위해 to 함수 사용하기`() {
        val map = mapOf(
            "a" to 1,
            "b" to 2,
            "c" to 2
        )
        println(map) // {a=1, b=2, c=2}
    }

    @Test
    fun `map of`() {
        val pair1: Pair<String, Int> = Pair("z", 1)
        val pair2: Pair<String, Int> = "a" to 1
    }
}
package com.example.kotlinjunit5

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SampleTest {

    var value = 0

    @Test
    @DisplayName("display name 기반으로 테스코드가 동작한다")
    internal fun `test code`() {
        then(1).isEqualTo(1)
        value++
        println("value: $value")
        println("this address : $this")
    }

    @Test
    internal fun `test code 2`() {
        then(1).isEqualTo(1)
        value++
        println("value: $value")
        println("this address : $this")
    }

    @Test
    @EnabledOnOs(OS.MAC, OS.WINDOWS)
    internal fun `EnabledOnOs`() {
        println("운영체제에 따라서 실행...")
        value++
        println("value: $value")
        println("this address : $this")
    }

    @Test
    @EnabledOnJre(JRE.JAVA_10, JRE.JAVA_11)
    internal fun `EnabledOnJre`() {
        println("JRE 버전에 따라서 실행...")
        value++
        println("value: $value")
        println("this address : $this")
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "local", matches = "local")
    internal fun EnabledOnIf() {
        println("Env 에따라서 실행...")
        value++
        println("value: $value")
        println("this address : $this")
    }


    @DisplayName("10번 반복 테스트")
    @RepeatedTest(value = 10, name = "{displayName}")
    internal fun `10번 반복 테스트`(repetitionInfo: RepetitionInfo) {
        println("test + ${repetitionInfo.currentRepetition} / ${repetitionInfo.totalRepetitions}}")
        value++
        println("value: $value")
        println("this address : $this")
    }


    @ParameterizedTest(name = "{index} {displayName} value={0}")
    @DisplayName("parameter test")
    @ValueSource(ints = [10, 20, 30])
    internal fun `parameter test`(int : Int) {
        println(int)
        value++
        println("value: $value")
        println("this address : $this")
    }



    //    companion object {
//        @BeforeAll
//        @JvmStatic
//        internal fun BeforeAll() {
//            println("BeforeAll : 테스트 실행되기 전 한번 실행됨")
//        }
//
//        @AfterAll
//        @JvmStatic
//        internal fun AfterAll() {
//            println("AfterAll : 테스트 실행된 후 한 번 실행됨")
//        }
//
//    }
//
//    @BeforeEach
//    internal fun BeforeEach() {
//        println("BeforeEach : 모든 테스트 마다 실행되기 전에실행됨")
//    }
//
//    @AfterEach
//    internal fun AfterEach() {
//        println("AfterEach : 모든 테스트 마다 실행된후 전에실행됨")
//    }
}


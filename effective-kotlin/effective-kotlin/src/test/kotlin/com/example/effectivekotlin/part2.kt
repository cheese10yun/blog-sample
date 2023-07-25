package com.example.effectivekotlin

import org.junit.jupiter.api.Test

class part2 {

    @Test
    fun name() {


    }

    class A {

        var name: String = ""
            get() = "123"
            set(value) {
                field = value
            }
    }

//    class Car(private var speed: Int) {
//        fun getSpeed() = speed
//        fun setSpeed(value: Int) { speed = value }
//    }

    class Car(speed: Int) {
        var speed = speed
            get() = field
            set(value) {
                field = value
            }
    }

    class AA(
        var name: String?
    ) {
        /*val name: String?

        init {
            when {
                name.isNullOrBlank().not() -> this.name = name
                else -> this.name = null
            }
        }*/
    }

    data class AAA(
        val name: String
    ) {

        // (컨트롤러) -> 서비스 -> 도메인
        // 계층간의 이동시 발리데이션 진행
        /**
         * 서비스 -> 서비스 -> 1차 진입을 위해 표현 계층을 통해 들어왔기 때문에 이미 검증이 완료 됐다고 가정한다.
         *
         * 각 계층간의 이동시 검증은 필요해 진다.
         *
         * 객체를 만드는건 비지니스 로직을 반영 해야한다.
         *
         * 예약일인데
         * 예약일을 과거로 할 수 없다. = 비지니스 로직인데, 이건 비지니스 영역이기 때문에 해당 도메인에서 책임을 지고 컨트롤 해야함
         * 문제가 뭐냐면, 각 객체의 책임을 본인이 다하지 않으면 그 책임이 다른 객체로 넘어감 ->
         *
         * 다른 서비스 영역에서 검증을 진행함 -> OOP -> 캡슐화가 저하됨
         *
         * 1. 객체라는것은 외부 객체와 협력을 해야하는데.
         *
         *
         *
         */

        init {
            if (this.name.isBlank()) {
                require(true)
            }
        }
    }
}
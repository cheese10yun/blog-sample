package com.example.kotlincoroutine


private val log = kLogger()

class FsmExample {

    fun execute(label: Int = 0) {

        var nextLabel: Int? = null

        when (label) {
            0 -> {
                log.info("Initial")
                nextLabel = 1
            }

            1 -> {
                log.info("State 2")
                nextLabel = 2
            }

            2 -> {
                log.info("State 3")
                nextLabel = 3
            }

            3 -> {
                log.info("End")
            }
        }

        if (nextLabel != null) {
            this.execute(nextLabel)
        }
    }
}

fun main() {
    val fsmExample = FsmExample()
    fsmExample.execute()
}
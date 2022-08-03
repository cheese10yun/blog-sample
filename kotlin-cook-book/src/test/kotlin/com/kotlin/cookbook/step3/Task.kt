package com.kotlin.cookbook.step3

class Task(
    val name: String,
    _priority: Int = DEFAULT_PRIORITY
) {

    companion object {
        const val MIN_PRIORITY = 1      // (1)
        const val MAX_PRIORITY = 1      // (1)
        const val DEFAULT_PRIORITY = 3  // (1)
    }

    var priority = validPriority(_priority) // (2)
        set(value) {
            field = validPriority(value)
        }

    private fun validPriority(p: Int) = // (3)
        p.coerceIn(MIN_PRIORITY, MAX_PRIORITY)
}


class Task1(val name: String) {
    var priority = 3
        set(value) {
            field = value.coerceIn(1..5)
        }

    val isLowPriority
        get() = priority < 3


    companion object {

        fun ad() {

            Task1(name = "name").apply { priority = 4 }
        }
    }
}
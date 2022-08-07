package com.kotlin.cookbook.step3

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test


class Test {

    @Test
    fun `load message`() {
        val customer = Customer("Fred").apply { message }
        then(customer.message).hasSize(3)

        customer.message
    }
}

//class Customer(
//    val name: String
//) {
//
//    private var _message: List<String>? = null // (1)
//
//    val message: List<String> // (2)
//        get() {  // (3)
//            if (_message == null) {
//                _message = loadMessage()
//            }
//            return _message!!
//        }
//
//    private fun loadMessage(): List<String> =
//        mutableListOf(
//            "Initial concat",
//            "Convinced them to use Kotlin",
//            "Sold training class. Sweet."
//        )
//            .also {
//                println("Loaded messages")
//            }
//}


class Customer(
    val name: String
) {

    val message: List<String> by lazy { loadMessage() }

    private fun loadMessage(): List<String> =
        mutableListOf(
            "Initial concat",
            "Convinced them to use Kotlin",
            "Sold training class. Sweet."
        )
            .also {
                println("Loaded messages")
            }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Customer

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

object MySingleton {
    val myProperty = 3

    fun myFunction() = "Hello"
}

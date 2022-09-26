package com.kotlin.cookbook.step8

import kotlin.properties.Delegates
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

interface Dialable {
    fun dial(number: String): String
}

class Phone : Dialable {
    override fun dial(number: String) =
        "Dialing $number..."
}

interface Snappable {
    fun takePicture(): String
}

class Camera : Snappable {
    override fun takePicture() =
        "Taking picture..."
}

class SmartPhone(
    private val phone: Dialable = Phone(),
    private val camera: Snappable = Camera()
) : Dialable by phone, Snappable by camera

class SmartPhoneTest {

    private val smartPhone: SmartPhone = SmartPhone()

    @Test
    fun `Dialing delegates to internal phone`() {
        then("Dialing 555-1234...").isEqualTo(smartPhone.dial("555-1234"))
    }

    @Test
    fun `Taking picture delegates to internal camera`() {
        then("Taking picture...").isEqualTo(smartPhone.takePicture())
    }

    @Test
    fun `test answer`() {
        println(ultimateAnswer)
        println(ultimateAnswer)
    }

    @Test
    fun `watched variable prints old nad new values`() {

        then(1).isEqualTo(watched)
        watched *= 2
        then(2).isEqualTo(watched)
        watched *= 2
//        then(4).isEqualTo(watched)
    }
}

val ultimateAnswer: Int by lazy {
    println("Computing the answer")
    42
}

var shouldNotBeNull: String by Delegates.notNull<String>()

var watched: Int by Delegates.observable(1) { prop, old, new ->
    println("${prop.name} changed from $old to $new")
}


var checked: Int by Delegates.vetoable(1) { prop, old, new ->
    println("Trying to change ${prop.name} from $old to $new")
    new >= 0
}
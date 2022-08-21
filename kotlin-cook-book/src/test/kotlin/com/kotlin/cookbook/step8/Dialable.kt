package com.kotlin.cookbook.step8

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
}
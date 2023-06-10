package com.example.intellijtest

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [MemberRegistrationFormValidator::class])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MemberRegistrationForm(
    val message: String = "Order sheet form is invalid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class MemberRegistrationFormValidator : ConstraintValidator<MemberRegistrationForm, MemberRegistrationRequest> {

    override fun isValid(value: MemberRegistrationRequest, context: ConstraintValidatorContext): Boolean {
        return true
    }

}
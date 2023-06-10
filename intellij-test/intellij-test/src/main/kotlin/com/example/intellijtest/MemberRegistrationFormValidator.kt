package com.example.intellijtest

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.springframework.stereotype.Service

@MustBeDocumented
@Constraint(validatedBy = [MemberRegistrationFormValidator::class])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MemberRegistrationForm(
    val message: String = "Order sheet form is invalid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Service
class MemberRegistrationFormValidator(
    private val memberRegistrationValidatorService: MemberRegistrationValidatorService
) : ConstraintValidator<MemberRegistrationForm, MemberRegistrationRequest> {

    override fun isValid(dto: MemberRegistrationRequest, context: ConstraintValidatorContext): Boolean {
        var inValidCount = 0
        val existedEmail = memberRegistrationValidatorService.isExistedEmail(dto.email)

        if (existedEmail) {
            inValidCount++
            addConstraintViolation(context, "${dto.email}은 이미 등록된 이메일 입니다.", "email")
        }

        // 기타 문제...
        if (true) {
            inValidCount++
            addConstraintViolation(context, "${dto.email}은 xxx 문제가 있습니다.", "email")
        }

        return inValidCount == 0
    }


    private fun addConstraintViolation(
        context: ConstraintValidatorContext,
        errorMessage: String,
        node: String
    ) {
        context.run {
            disableDefaultConstraintViolation()
            buildConstraintViolationWithTemplate(errorMessage)
                .addPropertyNode(node)
                .addConstraintViolation()
        }
    }
}


@Service
class MemberRegistrationValidatorService(
    private val memberQueryService: MemberQueryService
) {
    fun checkEmailDuplication(email: String) {
        var inValidCount = 0
        val errorMessage = StringBuilder()

        isExistedEmail(email)
            .also { isExistedEmail ->
                if (isExistedEmail) {
                    inValidCount++
                    errorMessage.append("${email}은 이미 등록된 이메일 입니다.\n")
                }
            }

        if (true) {
            inValidCount++
            errorMessage.append("${email}은 xxx 문제가 있습니다. \n")
        }

        check(inValidCount == 0) { errorMessage.toString() }
    }

    fun isExistedEmail(email: String) = memberQueryService.existedEmail(email)
}
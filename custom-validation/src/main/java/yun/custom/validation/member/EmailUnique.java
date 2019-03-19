package yun.custom.validation.member;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = EmailDuplicationValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailUnique {

  String message() default "Email is Duplication";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

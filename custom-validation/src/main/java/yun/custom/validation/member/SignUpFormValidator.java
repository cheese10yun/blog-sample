package yun.custom.validation.member;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SignUpFormValidator implements ConstraintValidator<SignUpForm, SignUpFormRequest> {

    @Override
    public void initialize(SignUpForm signUpForm) {
    }

    @Override
    public boolean isValid(SignUpFormRequest dto, ConstraintValidatorContext cxt) {
        int inValidCount = 0;

        if (dto.getEmail() == null) {
            cxt.disableDefaultConstraintViolation();
            cxt.buildConstraintViolationWithTemplate("Email is Null").addConstraintViolation();
            inValidCount++;
        }

        if (dto.getAge() == 0) {
            cxt.disableDefaultConstraintViolation();
            cxt.buildConstraintViolationWithTemplate("age is invalid").addPropertyNode("age").addConstraintViolation();
            inValidCount++;
        }

        if (dto.getMobile().getValue().length() == 11) {
            cxt.disableDefaultConstraintViolation();
            cxt.buildConstraintViolationWithTemplate("mobile is invalid").addPropertyNode("mobile").addConstraintViolation();
            inValidCount++;
        }

        if (dto.getName() == null) {
            cxt.disableDefaultConstraintViolation();
            cxt.buildConstraintViolationWithTemplate("name is Null").addConstraintViolation();
            inValidCount++;
        }

        return inValidCount == 0;
    }
}
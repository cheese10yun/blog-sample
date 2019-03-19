package yun.custom.validation.member;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailDuplicationValidator implements ConstraintValidator<EmailDuplication, String> {

    private final MemberRepository memberRepository;

    @Override
    public void initialize(EmailDuplication emailDuplication) {


    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext cxt) {

        boolean b = memberRepository.existsByEmail(email);


        if(email.length() == 5){
            cxt.disableDefaultConstraintViolation();
            cxt.buildConstraintViolationWithTemplate("email formatting is not" +
                    " valid")
                    .addConstraintViolation();

        }

        if (!b) {
            cxt.disableDefaultConstraintViolation();
            cxt.buildConstraintViolationWithTemplate("Email " + email +
                    "already exists!")
                    .addConstraintViolation();
        }


        return b;
    }
}

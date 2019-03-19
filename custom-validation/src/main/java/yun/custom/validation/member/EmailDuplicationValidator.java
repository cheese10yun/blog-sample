package yun.custom.validation.member;

import java.text.MessageFormat;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailDuplicationValidator implements ConstraintValidator<EmailUnique, String> {

  private final MemberRepository memberRepository;

  @Override
  public void initialize(EmailUnique emailUnique) {

  }

  @Override
  public boolean isValid(String email, ConstraintValidatorContext cxt) {

    boolean isExistEmail = memberRepository.existsByEmail(email);

    if (isExistEmail) {
      cxt.disableDefaultConstraintViolation();
      cxt.buildConstraintViolationWithTemplate(
          MessageFormat.format("Email {0} already exists!", email))
          .addConstraintViolation();
    }
    return !isExistEmail;
  }
}

# Custom Validation 어노테이션 만들기

스프링은 컨트롤러에서 클라이언트에서 넘겨받은 값에 대한 검증을 JSR-303 기반으로 쉽고 강력하게 할 수 있습니다. 또 한 커스텀 한 어 로테이션을 확장도 쉽게 구현할 수 있습니다.


아래에서 작성하는 어 로테이션은 해당 이메일이 유니크한지 검증을 하고 유니크하지 않은 이메일일 경우 Bad Request를 응답하는 어노테이션 입니다.


## 어노테이션 정의
```java
@Documented
@Constraint(validatedBy = EmailDuplicationValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailUnique {

  String message() default "Email is Duplication";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
```


## Validator 로직 작성
```java
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
```

넘겨 받은 email이 존재하는지 조회하고 중복되느 값이면 예외 메시지를 추가하고 `isValid(...)` 메서드에서 false를 리턴합니다. 


## Test

### API Code

```java
public class SignUpRequest {
    @EmailUnique @Email
    private String email;
}

public class MemberApi {

  private final MemberRepository memberRepository;

  @PostMapping
  public Member create(@RequestBody @Valid final SignUpRequest dto) {

    return memberRepository.save(Member.builder()
        .email(dto.getEmail())
        .build());
  }

}
```

### Test Code
```java
public class MemberApiTest {

  @Test
  public void signUp_test_이메일이_중복된_경우() throws Exception {
    //given
    final SignUpRequest dto = new SignUpRequest("yun@test.com");

    //when
    final ResultActions resultActions = requestSignUp(dto);

    //then
    resultActions
        .andExpect(status().isBadRequest());
  }

  private ResultActions requestSignUp(SignUpRequest dto) throws Exception {
    return mockMvc.perform(post("/members")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(dto)))
        .andDo(print());
  }

}
```


### Response

```json
{
    "timestamp": "2019-03-19T17:11:26.919+0000",
    "status": 400,
    "error": "Bad Request",
    "errors": [
        {
            "codes": [
                "EmailUnique.signUpRequest.email",
                "EmailUnique.email",
                "EmailUnique.java.lang.String",
                "EmailUnique"
            ],
            "arguments": [
                {
                    "codes": [
                        "signUpRequest.email",
                        "email"
                    ],
                    "arguments": null,
                    "defaultMessage": "email",
                    "code": "email"
                }
            ],
            "defaultMessage": "Email yun@test.com already exists!",
            "objectName": "signUpRequest",
            "field": "email",
            "rejectedValue": "yun@test.com",
            "bindingFailure": false,
            "code": "EmailUnique"
        }
    ],
    "message": "Validation failed for object='signUpRequest'. Error count: 1",
    "path": "/members"
}
```

테스트 코드를 실행해보면 EmailDuplicationValidator 로직이 정상 동작하는지 확인할 수 있습니다.
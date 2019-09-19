package yun.custom.validation.member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SignUpForm
public class SignUpFormRequest {

    private String email;

    private String name;

    private int age;

    private Mobile mobile;

}

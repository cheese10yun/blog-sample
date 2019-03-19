package yun.custom.validation.member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpRequest {


    @EmailDuplication
    private String email;

    public SignUpRequest(String email) {
        this.email = email;
    }


}

package com.yun.service.member.embedded;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {

    @Column(name = "password", nullable = false)
    private String value;

    @Builder
    public Password(final String value) {
        this.value = encodePassword(value);
    }


    public boolean isMatched(String rawPassword) {
        return this.value.equals(rawPassword);
    }

    public void change(String newPassword) {
        value = encodePassword(newPassword);
    }

    private String encodePassword(final String password) {
        //비밀번호 암호화 로직은 생략하겠습니다.
        return password;
    }


}

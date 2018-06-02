package com.yun.service.member;

import com.yun.service.member.embedded.*;
import lombok.*;

import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @EmbeddedId
    private MemberId id;

    @Embedded
    private Email email;

    @Embedded
    private Name name;

    @Embedded
    private PasswordExpiration passwordExpiration;

    @Embedded
    private Password password;

    @Builder
    public Member(Email email, Name name, Password password) {
        this.email = email;
        this.name = name;
        this.password = buildPassword(password);
        this.passwordExpiration = buildPasswordExpiration();
    }


    public void changePassword(String newPassword) {
        password.change(newPassword);
//        passwordExpiration.extend();
    }

    private Password buildPassword(Password password) {
        return Password.builder()
                .value(password.getValue())
                .build();
    }

    private PasswordExpiration buildPasswordExpiration() {
        return PasswordExpiration.builder().build();
    }

    public void updateName(Name name) {
        this.name = name;
    }
}

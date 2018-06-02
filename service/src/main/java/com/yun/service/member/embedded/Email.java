package com.yun.service.member.embedded;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {

    @org.hibernate.validator.constraints.Email
    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String valule;

}

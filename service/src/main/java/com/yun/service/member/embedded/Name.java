package com.yun.service.member.embedded;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Name {

    @Column(name = "first_name", nullable = false)
    private String first;

    @Column(name = "last_name", nullable = false)
    private String last;
}

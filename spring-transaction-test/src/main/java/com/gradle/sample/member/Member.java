package com.gradle.sample.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private long id;
    private String name;
    private String email;

    @Builder
    public Member(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}

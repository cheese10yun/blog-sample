package com.cheese.embedded.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Name {

    @Column(name = "first_name")
    private String first;

    @Column(name = "last_name")
    private String last;

    @Builder
    public Name(String first, String last) {
        this.first = first;
        this.last = last;
    }

    public String getFullName() {
        return this.first +", "+ this.last;
    }
}

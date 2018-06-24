package com.restdocs.sample.member;

import com.restdocs.sample.model.Address;
import com.restdocs.sample.model.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id
    @GeneratedValue
    private long id;

    @Embedded
    private Email email;

    @Embedded
    private Address address;

    @Builder
    public Member(Email email, Address address) {
        this.email = email;
        this.address = address;
    }
}

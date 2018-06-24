package com.restdocs.sample.member.dto;

import com.restdocs.sample.member.Member;
import com.restdocs.sample.model.Address;
import com.restdocs.sample.model.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SignUpDto {

    @Valid
    private Email email;

    @Valid
    private Address address;

    @Builder
    public SignUpDto(Email email, Address address) {
        this.email = email;
        this.address = address;
    }

    public Member toEntity() {

        return Member.builder()
                .email(email)
                .address(address)
                .build();

    }
}

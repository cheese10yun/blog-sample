package com.restdocs.sample.member.dto;

import com.restdocs.sample.member.Member;
import com.restdocs.sample.model.Address;
import com.restdocs.sample.model.Email;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberResponseDto {
    private Email email;
    private Address address;

    @Builder
    public MemberResponseDto(Member member) {
        this.email = member.getEmail();
        this.address = member.getAddress();
    }

}

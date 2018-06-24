package com.restdocs.sample.member;

import com.restdocs.sample.member.dto.SignUpDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;


    public Member signUp(SignUpDto dto) {
        final Member member = dto.toEntity();

        return memberRepository.save(member);
    }
}

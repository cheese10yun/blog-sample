package com.yun.service.member.service;

import com.yun.service.member.Member;
import com.yun.service.member.MemberRepository;
import com.yun.service.member.embedded.Email;
import com.yun.service.member.embedded.MemberId;
import com.yun.service.member.exception.MemberNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class MemberFindService {

    private MemberRepository memberRepository;

    public Member findById(final MemberId id) {
        final Member member = memberRepository.findOne(id);
        if (member == null) throw new MemberNotFoundException(id);
        return member;
    }

    public Member findByEmail(final Email email) {
        final Member member = memberRepository.findByEmail(email);
        if (member == null) throw new MemberNotFoundException(email);
        return member;
    }
}

package com.yun.service.member.service;

import com.yun.service.member.dto.PasswordDto;
import com.yun.service.member.embedded.Email;
import com.yun.service.member.Member;
import com.yun.service.member.embedded.MemberId;
import com.yun.service.member.MemberRepository;
import com.yun.service.member.embedded.Name;
import com.yun.service.member.exception.MemberNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {

    private MemberRepository memberRepository;


    @Override
    public Member findById(MemberId id) {
        final Member member = memberRepository.findOne(id);
        if (member == null) throw new MemberNotFoundException(id);
        return member;
    }

    @Override
    public Member findByEmail(Email email) {
        final Member member = memberRepository.findByEmail(email);
        if (member == null) throw new MemberNotFoundException(email);
        return member;
    }

    @Override
    public void changePassword(PasswordDto.ChangeRequest dto) {
//        final MemberId id = dto.getId();
//        final Member member = findById(id);
//        final String newPassword = dto.getNewPassword().getValue();
//        final String password = dto.getPassword().getValue();
//
//        if (!member.getPassword().isMatched(password))
//            throw new IllegalArgumentException("password is not matched");

//        member.changePassword(newPassword);
    }

    @Override
    public Member updateName(MemberId id, Name name) {
        final Member member = findById(id);
        member.updateName(name);
        return member;
    }
}

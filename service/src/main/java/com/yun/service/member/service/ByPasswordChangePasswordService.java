package com.yun.service.member.service;

import com.yun.service.member.Member;
import com.yun.service.member.dto.PasswordDto;
import com.yun.service.member.embedded.MemberId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class ByPasswordChangePasswordService implements ChangePasswordService {
    private MemberFindService memberFindService;

    @Override
    public void change(MemberId id, PasswordDto.ChangeRequest dto) {
        if (dto.getPassword().equals("비밀번호가 일치하는지 판단 로직...")) {
            final Member member = memberFindService.findById(id);
            final String newPassword = dto.getNewPassword().getValue();
            member.changePassword(newPassword);
        }
    }
}

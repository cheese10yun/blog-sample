package com.yun.service.member.service;

import com.yun.service.member.Member;
import com.yun.service.member.dto.PasswordDto;
import com.yun.service.member.embedded.MemberId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ByAuthChangePasswordService implements ChangePasswordService {

    private MemberFindService memberFindService;

    @Override
    public void change(MemberId id, PasswordDto.ChangeRequest dto) {
        if (dto.getAuthCode().equals("인증 코드가 적합한지 로직 추가...")) {

            final Member member = memberFindService.findById(id);
            final String newPassword = dto.getNewPassword().getValue();
            member.changePassword(newPassword);
            // 필요로직...
        }
    }
}

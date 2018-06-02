package com.yun.service.member;

import com.yun.service.member.dto.PasswordDto;
import com.yun.service.member.service.ByAuthChangePasswordService;
import com.yun.service.member.service.ByPasswordChangePasswordService;
import com.yun.service.member.service.ChangePasswordService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MemberFactory {

    private ByAuthChangePasswordService byAuthChangePasswordService;
    private ByPasswordChangePasswordService byPasswordChangePasswordService;


    public ChangePasswordService getChangePasswordInstance(PasswordDto.ChangeRequest dto) {

        if (dto.getAuthCode() != null) return byAuthChangePasswordService;
        if (dto.getPassword() != null) return byPasswordChangePasswordService;

        throw new IllegalArgumentException("not found instance");
    }
}

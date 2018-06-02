package com.yun.service.member.service;

import com.yun.service.member.dto.PasswordDto;
import com.yun.service.member.embedded.MemberId;

public interface ChangePasswordService {

    public void change(MemberId id, PasswordDto.ChangeRequest dto);

}

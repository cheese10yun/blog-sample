package com.yun.service.member.service;

import com.yun.service.member.dto.PasswordDto;
import com.yun.service.member.embedded.Email;
import com.yun.service.member.Member;
import com.yun.service.member.embedded.MemberId;
import com.yun.service.member.embedded.Name;

public interface MemberService {

    Member findById(MemberId id);

    Member findByEmail(Email email);

    void changePassword(PasswordDto.ChangeRequest dto);

    Member updateName(MemberId id, Name name);
}

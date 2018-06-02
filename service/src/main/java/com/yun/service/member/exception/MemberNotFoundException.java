package com.yun.service.member.exception;

import com.yun.service.member.embedded.Email;
import com.yun.service.member.embedded.MemberId;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(MemberId id) {
        super(id.getValue() + "is not found");
    }


    public MemberNotFoundException(Email email) {
        super(email.getValule() + "is not found");
    }
}

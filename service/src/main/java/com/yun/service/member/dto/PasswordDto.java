package com.yun.service.member.dto;

import com.yun.service.member.embedded.MemberId;
import com.yun.service.member.embedded.Password;
import lombok.*;

public class PasswordDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChangeRequest {
        private String authCode;
        private Password password;
        private Password newPassword;
    }

//    @Getter
//    @AllArgsConstructor
//    @Builder
//    public static class Change {
//        private MemberId id;
//        private String authCode;
//        private Password password;
//        private Password newPassword;
//    }
}

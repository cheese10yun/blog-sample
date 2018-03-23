package com.cheese.notification.kakao;

import lombok.Builder;
import lombok.Getter;

public class KakaoNotificationDto {

    @Getter
    public static class Creation {
        private String subject;
        private String content;
        private String templateCode;
        private String mobile;

        @Builder
        public Creation(String subject, String content, String templateCode, String mobile) {
            this.subject = subject;
            this.content = content;
            this.templateCode = templateCode;
            this.mobile = mobile;
        }

        public KakaoNotification toEntity() {
            return KakaoNotification.builder()
                    .subject(this.subject)
                    .content(this.content)
                    .templateCode(this.templateCode)
                    .build();
        }
    }
}

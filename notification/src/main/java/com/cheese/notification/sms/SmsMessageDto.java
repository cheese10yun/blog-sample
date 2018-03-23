package com.cheese.notification.sms;

import lombok.Builder;

public class SmsMessageDto {

    public static class Creation {
        private String from = "07043490013"; //발신자 번호
        private String to; //수신자 번호
        private String text;

        @Builder
        public Creation(String to, String text) {
            this.to = to;
            this.text = text;
        }
    }
}

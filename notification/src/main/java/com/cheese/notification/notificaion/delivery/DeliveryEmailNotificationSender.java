package com.cheese.notification.notificaion.delivery;

import org.springframework.stereotype.Component;

@Component
public class DeliveryEmailNotificationSender implements DeliveryNotificationSender {

    @Override
    public void send(DeliveryMessageDto.Message dto) {
        // 이메일 보내는 로직...
    }
}

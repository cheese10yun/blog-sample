package com.cheese.notification.notificaion.delivery;

public interface DeliveryNotificationSender {

    void send(DeliveryMessageDto.Message dto);
}

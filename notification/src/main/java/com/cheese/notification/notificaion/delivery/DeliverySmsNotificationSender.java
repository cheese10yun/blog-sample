package com.cheese.notification.notificaion.delivery;

import com.cheese.notification.sms.SmsMessageDto;
import com.cheese.notification.sms.SmsNotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeliverySmsNotificationSender  implements DeliveryNotificationSender {

    private final SmsNotificationSender smsNotificationSender;

    @Autowired
    public DeliverySmsNotificationSender(SmsNotificationSender smsNotificationSender) {
        this.smsNotificationSender = smsNotificationSender;
    }

    @Override
    public void send(DeliveryMessageDto.Message dto) {
        smsNotificationSender.sendSMS(buildSmsMessageDto(dto));
    }

    private SmsMessageDto.Creation buildSmsMessageDto(DeliveryMessageDto.Message dto) {
        return SmsMessageDto.Creation.builder()
                .text(writeContent(dto))
                .to(dto.getReceiver().getMobile())
                .build();
    }

    private String writeContent(DeliveryMessageDto.Message dto) {
        return dto.getSender().getName() + " 님이 보내주신 물품이 도착완료 했습니다.";
    }


}


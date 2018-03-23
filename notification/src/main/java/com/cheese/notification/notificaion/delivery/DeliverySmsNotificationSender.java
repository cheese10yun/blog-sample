package com.cheese.notification.notificaion.delivery;

import com.cheese.notification.sms.SmsMessageDto;
import com.cheese.notification.sms.SmsNotificationSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DeliverySmsNotificationSender extends SmsNotificationSender implements DeliveryNotificationSender {

    public DeliverySmsNotificationSender(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public void send(DeliveryMessageDto.Message dto) {
        sendSMS(buildSmsMessageDto(dto));
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


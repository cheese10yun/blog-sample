package com.cheese.notification.notificaion.delivery;

import com.cheese.notification.delivery.DeliveryNotificationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeliveryNotificationSenderFactory {

    private final DeliveryKakaoNotificationSender deliveryKakaoNotificationSender;
    private final DeliverySmsNotificationSender deliverySmsNotificationSender;
    private final DeliveryEmailNotificationSender deliveryEmailNotificationSender;

    @Autowired
    public DeliveryNotificationSenderFactory(DeliveryKakaoNotificationSender deliveryKakaoNotificationSender, DeliverySmsNotificationSender deliverySmsNotificationSender, DeliveryEmailNotificationSender deliveryEmailNotificationSender) {
        this.deliveryKakaoNotificationSender = deliveryKakaoNotificationSender;
        this.deliverySmsNotificationSender = deliverySmsNotificationSender;
        this.deliveryEmailNotificationSender = deliveryEmailNotificationSender;
    }

    public DeliveryNotificationSender getInstanceByType(DeliveryNotificationTypeEnum type) {
        final DeliveryNotificationSender deliveryNotificationSender;

        switch (type) {
            case SMS:
                deliveryNotificationSender = deliverySmsNotificationSender;
                break;
            case EMAIL:
                deliveryNotificationSender = deliveryEmailNotificationSender;
                break;
            case KAKAO:
                deliveryNotificationSender = deliveryKakaoNotificationSender;
                break;
            default:
                throw new IllegalArgumentException("error...");
        }
        return deliveryNotificationSender;
    }
}

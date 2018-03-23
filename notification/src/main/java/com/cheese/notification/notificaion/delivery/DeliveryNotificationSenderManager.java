package com.cheese.notification.notificaion.delivery;

import com.cheese.notification.delivery.Delivery;
import com.cheese.notification.delivery.DeliveryNotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryNotificationSenderManager {


    private final DeliveryNotificationSenderFactory deliveryNotificationSenderFactory;

    @Autowired
    public DeliveryNotificationSenderManager(DeliveryNotificationSenderFactory deliveryNotificationSenderFactory) {
        this.deliveryNotificationSenderFactory = deliveryNotificationSenderFactory;
    }

    public void sendMessage(Delivery delivery) {
        final List<DeliveryNotificationType> notifications = delivery.getDeliveryNotificationTypes();

        if (!notifications.isEmpty())
            for (DeliveryNotificationType type : notifications)
                getInstanceByType(type).send(buildMessage(delivery));
    }

    private DeliveryMessageDto.Message buildMessage(Delivery delivery) {
        return DeliveryMessageDto.Message.builder()
                .delivery(delivery)
                .build();
    }

    private DeliveryNotificationSender getInstanceByType(DeliveryNotificationType type) {
        return deliveryNotificationSenderFactory.getInstanceByType(type.getType());
    }
}

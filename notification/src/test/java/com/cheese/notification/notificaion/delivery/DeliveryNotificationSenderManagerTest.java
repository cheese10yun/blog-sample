package com.cheese.notification.notificaion.delivery;

import com.cheese.notification.delivery.Delivery;
import com.cheese.notification.delivery.DeliveryNotificationType;
import com.cheese.notification.delivery.DeliveryNotificationTypeEnum;
import com.cheese.notification.email.EmailNotificationSender;
import com.cheese.notification.kakao.KakaoNotificaionSender;
import com.cheese.notification.kakao.KakaoNotificationRepository;
import com.cheese.notification.receiver.Receiver;
import com.cheese.notification.sender.Sender;
import com.cheese.notification.sms.SmsNotificationSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeliveryNotificationSenderManagerTest {


    private DeliveryNotificationSenderManager deliveryNotificationSenderManager;
    private DeliveryNotificationSenderFactory deliveryNotificationSenderFactory;

    @Mock
    private DeliveryKakaoNotificationSender deliveryKakaoNotificationSender;

    @Mock
    private DeliverySmsNotificationSender deliverySmsNotificationSender;

    @Mock
    private DeliveryEmailNotificationSender deliveryEmailNotificationSender;


    private Delivery delivery;

    @Before
    public void setUp() throws Exception {
        delivery = buildDelivery();

        deliveryNotificationSenderFactory = new DeliveryNotificationSenderFactory(deliveryKakaoNotificationSender, deliverySmsNotificationSender, deliveryEmailNotificationSender);

        deliveryNotificationSenderManager = new DeliveryNotificationSenderManager(deliveryNotificationSenderFactory);

    }

    @Test
    public void sendMessage() {

        //given

        //when
        deliveryNotificationSenderManager.sendMessage(delivery);

        //then
        verify(deliveryKakaoNotificationSender, atLeastOnce()).send(any(DeliveryMessageDto.Message.class));
        verify(deliverySmsNotificationSender, atLeastOnce()).send(any(DeliveryMessageDto.Message.class));




    }

    private Delivery buildDelivery() {
        return Delivery.builder()
                .itemName("DDD Start : 도메인 주도 설계 구현과 핵심 개념 익히기")
                .itemPrice(2500)
                .deliveryNotificationTypes(buildDeliveryNotificationType())
                .receiver(buildReceiver())
                .sender(buildSender())
                .build();
    }


    private List<DeliveryNotificationType> buildDeliveryNotificationType() {

        List<DeliveryNotificationType> types = new ArrayList<>();
        types.add(buildType(DeliveryNotificationTypeEnum.KAKAO));
        types.add(buildType(DeliveryNotificationTypeEnum.SMS));
        return types;
    }

    private DeliveryNotificationType buildType(DeliveryNotificationTypeEnum kakao) {
        return DeliveryNotificationType.builder()
                .type(kakao)
                .build();
    }


    private Sender buildSender() {
        return Sender.builder()
                .contact("02-XXX-XXXX")
                .name("교보문고")
                .build();
    }

    private Receiver buildReceiver() {
        return Receiver.builder()
                .email("cheese10yun@gmail.com")
                .location("서울시 XXXXX XXXXX")
                .mobile("010-XXXX-XXXX")
                .name("Yun")
                .build();
    }
}
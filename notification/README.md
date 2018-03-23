---
layout: post
title: Spring OOP 프로그래밍 예제
subtitle: 내가 해석한 SOLID
catalog: true
header-img: https://i.imgur.com/avC1Xor.jpg
thumbnail: https://i.imgur.com/PI6g3ku.png
date:
tags:
  -
---

![](https://i.imgur.com/PI6g3ku.png)

**해당 요구사항에 맞는 객체지향 프로그래밍을 진행하고 해당 코드를 SOLD 원칙에 맞게 제 나름대로 해석 해보았습니다. 아직 배우는 단계라 너무 부족합니다. 지적 사항을 댓글로 남겨주시면 정말로 감사하겠습니다.** 위사진은 해당 셈플코드의 간략한 클래스 다이어그램 입니다.


## 요구사항

* 배송이 완료 됬을 경우 사용자가 등록한 알림을 받는다.
* 알림에는 SMS, Kakao, Email 이 있다
* 알람은 지속적으로 추가 될 가능 성이 높아 보인다.


## Delivery Domain

```java
public class Delivery {
    ...

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private List<DeliveryNotificationType> deliveryNotificationTypes = new ArrayList<>();

    @OneToOne(targetEntity = Sender.class)
    @Column(name = "sender_id")
    private Sender sender;

    @OneToOne(targetEntity = Receiver.class)
    @Column(name = "receiver_id")
    private Receiver receiver;

    ...
}
```
* Delivery는 Sender(발송인), Receiver(수신자)와 연관관계를 맺는다.
* Delivery는 배송이 안료되면 수신자에게 보낼 알림 타입의 리스트 DeliveryNotificationType와 연관관계를 맺는다


## DeliveryNotificationSender Interface
```java
public interface DeliveryNotificationSender {
    void send(DeliveryMessageDto.Message dto);
}
```
* 배송 노티를 보내는 인터페이스
* dto 클래스로 유연하게 받을 수 있게 처리

## DeliveryKakaoNotificationSender 구현 클래스
```java
@Component
public class DeliveryKakaoNotificationSender extends KakaoNotificaionSender implements DeliveryNotificationSender {
    ...
    @Override
    public void send(DeliveryMessageDto.Message dto) {
        create(buildKaKaoNotificationDto(dto));
    }
    ...
}
```
* 배송 관련 카카오 메시지를 담당하는 클래스
* `DeliveryNotificationSender` 인터페이스를 `implements` 해서 자신이 `send` 메서드를 구현 해야하는 책임을 명확하게 알 수 있다.
* `KakaoNotificaionSender` 클래스를 상속 받고 있어 실제 구체적으로 어떻게 보내지는지 모르더라도 상관 없다.
* 테이블에 insert 되고 그 데이터 기반으로 카카오에서 메시지를 전송해 준다.


## DeliverySmsNotificationSender 구현 클래스

```java
@Component
public class DeliverySmsNotificationSender extends SmsNotificationSender implements DeliveryNotificationSender {
    ...
    @Override
    public void send(DeliveryMessageDto.Message dto) {
        sendSMS(buildSmsMessageDto(dto));
    }
    ...
}

```
* 배송 관련 카카오 메시지를 담당하는 클래스이다.
* `DeliveryNotificationSender` 인터페이스를 `implements` 해서 자신이 `send` 메서드를 구현 해야하는 책임을 명확하게 알 수 있다.
* `SmsNotificationSender` 클래스를 상속 받고 있어 실제 구체적으로 어떻게 보내지는지 모르더라도 상관 없다.
* 해당 업체 API 호출을 통해서 문자전송이 진행된다. 카카오 메시지 전송과는 다르다. 하지만 `DeliveryNotificationSender` 인터페이스를 통해서  `send`라는 동일한 책임을 갖게 된다.

## DeliveryNotificationSenderManager

```java
public class DeliveryNotificationSenderManager {
    ...
    public void sendMessage(Delivery delivery) {
        final List<DeliveryNotificationType> notifications = delivery.getDeliveryNotificationTypes();

        if (!notifications.isEmpty())
            for (DeliveryNotificationType type : notifications)
                getInstanceByType(type).send(buildMessage(delivery));
    }

    private DeliveryNotificationSender getInstanceByType(DeliveryNotificationType type) {
        return deliveryNotificationSenderFactory.getInstanceByType(type.getType());
    }
    ...
}
```
* 사용자가 등록한 알림을 전송
* **`getInstanceByType` 팩토리 메소드를 통해서 해당 타입에 맞는 인스턴스가 의존성 주입**
    - **type이 SMS 일 경우 `DeliverySmsNotificationSender` 인스턴스 주입**
    - **`DeliverySmsNotificationSender` 인스턴스 `send`(SMS 전송) 메서드 실행**
* ***컴파일 단계에서는 알림을 보내는 것이 `DeliveryNotificationSender` 인터페이스를 바라보지만 런타임 단계에서는 그것이 역전되 IoC 발생***

## SOLID

아직 배워가는 단계라 많이 부족 하지만 나름대로 SOLD 원측에 의해서 정리 해보겠습니다.

### SRP : 단일 책임 원칙

* 객체는 오직 하나의 변경의 이유만을 갖게되 었습니다. 배송 관련 카카오 전송시 세부 메시지 형태나, 포함될 정보가 달라질 경우 `DeliveryKakaoNotificationSender` 클래스만 변경 하면됩니다.
* 다른 곳에서 다른형태로 카카오 메시지를 전송하고 있는 로직에 영향을 미치지 않습니다.

### OCP : 개방-폐쇄 원칙

* 확장에는 열려 있습니다.
    - 새로운 배송 라인 알림이 추가 된다고 하면 `DeliveryNotificationSender` 인터페이스를 `implements` 하여 `send` 메서드만 세부 구현 하면됩니다.
    -  IoC를 이용해서 `send` 메서드를 다형성으로 해결하지 않았다면 알림이 추가 될때 마다 `if` 으로 무슨 알림이면 무슨 `send`를 실행 하라는 반복 적인 코드들이 나오게 됩니다.
    - `if`은 직관적으로 이해하기 편하 코드지만 유지보수하기는 어렵습니다. 만약 10개 알림이 있고 11번째 알림을 추가 한다고 가정했을 때 앞에 작성된 `if`문들을 이해하고 또 11 번째 `if`문을 추가해야 합니다. 코드는 작성하는 시간보다 읽히는 시간이 더 많습니다.
* 변경에는 닫혀 있습니다.
    - 새로운 배송 알림이 추가 되더라도 기존 코드의 변경은 거의 없습니다. 팩토리 메소드에 새로운 배송 알림을 담당하는 인스턴스를 추가 하기만 하면 됩니다.
    - 위에서 언급했듯이 새로운 배송 알림이 추가되면 해당 코드에 가서 `if`을 통한 `send` 메시지를 작성할 필요가 없습니다.

### LSP : 리스코프 치환 원칙
* 서브 타입은 언제나 슈퍼타입을 교체 할 수 있어야 한다.
* `DeliveryKakaoNotificationSender` 서브 클래스는 `KakaoNotificaionSender`  슈퍼 클래스를 대체 합니다. 카카오 알림 중에서 배송 관련 알림을 교체해서 보내줍니다. **Is a 관계가 성립됩니다.**


### ISP : 인터페이스 분리 원칙
* 서브 타입에서 상위 타입의 메소드들의 사용하지 않는 경우가 없

### DIP : 의존성 역전 원칙
* `DeliveryNotificationSenderManager` 클래스에서 팩토리 메소드를 통해서  `DeliveryNotificationSender`의 새부 인스턴스를 각 타입에 맞게 변경 해주고 있습니다.




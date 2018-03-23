package com.cheese.notification.delivery;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DeliveryNotificationType {

    @Id
    @GeneratedValue
    private long id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "delivery_id", nullable = false, updatable = false)
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DeliveryNotificationTypeEnum type;

    @Builder
    public DeliveryNotificationType(Delivery delivery, DeliveryNotificationTypeEnum type) {
        this.delivery = delivery;
        this.type = type;
    }
}


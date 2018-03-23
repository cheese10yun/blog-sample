package com.cheese.notification.delivery;

import com.cheese.notification.receiver.Receiver;
import com.cheese.notification.sender.Sender;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Delivery {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_price")
    private int itemPrice;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private List<DeliveryNotificationType> deliveryNotificationTypes = new ArrayList<>();

    @OneToOne(targetEntity = Sender.class)
    @Column(name = "sender_id")
    private Sender sender;

    @OneToOne(targetEntity = Receiver.class)
    @Column(name = "receiver_id")
    private Receiver receiver;

    @Builder
    public Delivery(String itemName, int itemPrice, List<DeliveryNotificationType> deliveryNotificationTypes, Sender sender, Receiver receiver) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.deliveryNotificationTypes = deliveryNotificationTypes;
        this.sender = sender;
        this.receiver = receiver;
    }
}

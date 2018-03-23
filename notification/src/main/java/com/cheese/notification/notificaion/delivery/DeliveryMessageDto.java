package com.cheese.notification.notificaion.delivery;

import com.cheese.notification.receiver.Receiver;
import lombok.Builder;
import lombok.Getter;

public class DeliveryMessageDto {

    @Getter
    public static class Message {
        private Delivery delivery;
        private Sender sender;
        private Receiver receiver;

        @Builder
        public Message(com.cheese.notification.delivery.Delivery delivery) {
            this.delivery = buildDelivery(delivery);
            this.sender = buildSender(delivery.getSender());
            this.receiver = buildReceiver(delivery.getReceiver());

        }

        private Receiver buildReceiver(com.cheese.notification.receiver.Receiver receiver) {
            return Receiver.builder()
                    .email(receiver.getEmail())
                    .mobile(receiver.getMobile())
                    .name(receiver.getName())
                    .build();
        }

        private Sender buildSender(com.cheese.notification.sender.Sender sender) {
            return Sender.builder()
                    .name(sender.getName())
                    .contact(sender.getContact())
                    .build();
        }

        private Delivery buildDelivery(com.cheese.notification.delivery.Delivery delivery) {
            return Delivery.builder()
                    .itemName(delivery.getItemName())
                    .build();
        }
    }


    @Getter
    public static class Delivery {
        private String itemName;

        @Builder
        public Delivery(String itemName) {
            this.itemName = itemName;
        }
    }


    @Getter
    public static class Receiver {
        private final String name;
        private final String email;
        private final String mobile;
        private final String location;

        @Builder
        public Receiver(String name, String email, String mobile, String location) {
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.location = location;
        }
    }

    @Getter
    public static class Sender {
        private String name;
        private String contact;

        @Builder
        public Sender(String name, String contact) {
            this.name = name;
            this.contact = contact;
        }
    }

}

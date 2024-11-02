package com.example.kafka;

import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PizzaMessage {
    // 피자 메뉴를 설정. getRandomValueFromList()에서 임의의 피자명을 출력하는 데 사용.
    private static final List<String> pizzaNames = List.of("Potato Pizza", "Cheese Pizza",
            "Cheese Garlic Pizza", "Super Supreme", "Peperoni");
//    private static final List<String> pizzaNames = List.of("고구마 피자", "치즈 피자",
//            "치즈 갈릭 피자", "슈퍼 슈프림", "페페로니 피자");

    // 피자 가게명을 설정. getRandomValueFromList()에서 임의의 피자 가게명을 출력하는데 사용.
    private static final List<String> pizzaShop = List.of(
            "A001", "B001", "C001", "D001", "E001", "F001", "G001", "H001",
            "I001", "J001", "K001", "L001", "M001", "N001", "O001", "P001", "Q001"
    );

    public PizzaMessage() {
    }

    //인자로 피자명 또는 피자가게 List와 Random 객체를 입력 받아서 random한 피자명 또는 피자 가게 명을 반환.
    private String getRandomValueFromList(List<String> list, Random random) {
        int size = list.size();
        int index = random.nextInt(size);

        return list.get(index);
    }

    //random한 피자 메시지를 생성하고, 피자가게 명을 key로 나머지 정보를 value로 하여 Hashmap을 생성하여 반환.
    public HashMap<String, String> produce_msg(Faker faker, Random random, int id) {

        String shopId = getRandomValueFromList(pizzaShop, random);
        String pizzaName = getRandomValueFromList(pizzaNames, random);

        String ordId = "ord" + id;
        String customerName = faker.name().fullName();
        String phoneNumber = faker.phoneNumber().phoneNumber();
        String address = faker.address().streetAddress();
        LocalDateTime now = LocalDateTime.now();
        String message = String.format("order_id:%s, shop:%s, pizza_name:%s, customer_name:%s, phone_number:%s, address:%s, time:%s"
                , ordId, shopId, pizzaName, customerName, phoneNumber, address
                , now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.KOREAN)));
        //System.out.println(message);
        HashMap<String, String> messageMap = new HashMap<>();
        messageMap.put("key", shopId);
        messageMap.put("message", message);

        return messageMap;
    }

    public static void main(String[] args) {
        PizzaMessage pizzaMessage = new PizzaMessage();
        // seed값을 고정하여 Random 객체와 Faker 객체를 생성.
        long seed = 2022;
        Random random = new Random(seed);
        Faker faker = Faker.instance(random);

        for (int i = 0; i < 60; i++) {
            HashMap<String, String> message = pizzaMessage.produce_msg(faker, random, i);
            System.out.println("key:" + message.get("key") + " message:" + message.get("message"));
        }

    }
}

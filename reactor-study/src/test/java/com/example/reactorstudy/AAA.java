package com.example.reactorstudy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class AAA {

    @Test
    void name() {

        final List<Order> orders = new ArrayList<>();

        orders.add(new Order(Status.READY, LocalDate.of(2020, 1, 12)));
        orders.add(new Order(Status.PROCESSING, LocalDate.of(2020, 1, 12)));
        orders.add(new Order(Status.COMPLETED, LocalDate.of(2020, 1, 12)));

        orders.add(new Order(Status.READY, LocalDate.of(2020, 2, 12)));
        orders.add(new Order(Status.PROCESSING, LocalDate.of(2020, 2, 12)));
        orders.add(new Order(Status.COMPLETED, LocalDate.of(2020, 2, 12)));

        orders.add(new Order(Status.READY, LocalDate.of(2020, 3, 12)));
        orders.add(new Order(Status.PROCESSING, LocalDate.of(2020, 3, 12)));
        orders.add(new Order(Status.COMPLETED, LocalDate.of(2020, 3, 12)))

        ;orders.add(new Order(Status.READY, LocalDate.of(2020, 1, 1)));
        orders.add(new Order(Status.PROCESSING, LocalDate.of(2020, 3, 12)));
        orders.add(new Order(Status.COMPLETED, LocalDate.of(2020, 1, 1)));

//        final Map<Status, List<Order>> collect = orders.stream()
//            .sorted(Comparator.comparing(Order::getStartDate))
//            .collect(Collectors.groupingBy(Order::getStatus));
//
//        final List<Order> newOrders = new ArrayList<>();
//
//        newOrders.add(collect.get(Status.READY).get(0));
//        newOrders.add(collect.get(Status.PROCESSING).get(0));
//        newOrders.add(collect.get(Status.COMPLETED).get(0));
//
//        for (Order newOrder : newOrders) {
//            System.out.println(newOrder);
//        }
//
        final List<Order> collect = orders.stream()
            .sorted(Comparator.comparing(Order::getStartDate))
            .collect(
                Collectors.groupingBy(Order::getStatus,
                    Collectors.collectingAndThen(Collectors.toList(), values -> values.get(0)))
            )
            .values()
            .stream()
            .collect(Collectors.toList());

        System.out.println(collect);


    }
}

package com.example.jpafetchjoin;

import com.example.jpafetchjoin.address.Address;
import com.example.jpafetchjoin.order.Order;
import com.example.jpafetchjoin.order.OrderRepository;
import com.example.jpafetchjoin.product.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppRunner implements ApplicationRunner {

  private final OrderRepository orderRepository;


  @Override
  public void run(ApplicationArguments args) {

    final List<Order> orders = new ArrayList<>();

    IntStream.range(0, 30).forEach(i -> {
      final Order order = new Order("yun");
      order.addAddress(new Address("서울", order));
      order.addProduct(new Product("양물", order));
      orders.add(order);

    });
    orderRepository.saveAll(orders);

  }
}

package com.example.jpafetchjoin.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderApi {

  private final OrderRepository orderRepository;

  @GetMapping
  public Page<Order> getOrderAll(Pageable pageable){
    return orderRepository.findAll(pageable);
  }

  @GetMapping("/all")
  public Page<Order> getOrders(Pageable pageable){
    return orderRepository.findByPageWithAll(pageable);
  }

  @GetMapping("/address")
  public Page<Order> getOrdersWithAddress(Pageable pageable){
    return orderRepository.findByPageWithAddress(pageable);
  }

  @GetMapping("/products")
  public Page<Order> getOrdersWithProducts(Pageable pageable){
    return orderRepository.findByPageWithProducts(pageable);
  }

}

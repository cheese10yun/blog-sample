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


  @GetMapping("/products")
  public Page<Order> getOrdersWithProducts(Pageable pageable){
    return orderRepository.findByPageWithProducts(pageable);
  }

}

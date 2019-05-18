package yun.blog.transactionstudy.order;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderApi {

  private final OrderService orderService;

  @GetMapping
  public List<Order> get() {
    return orderService.findAll();
  }

  @PostMapping
  public Order create() {
    final Order order = orderService.create();
    return order;

  }



}

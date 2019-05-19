package yun.blog.transactionstudy.order;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderFailedService orderFailedService;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Order create() {

    Order order = save();
    orderFailedService.fail();

    return order;

  }

  public Order save() {
    final Order order = orderRepository.save(new Order(OrderStatus.READY));
//    if (true) {
//      throw new RuntimeException();
//    }
    return order;
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Order fail() {
    final Order order = orderRepository.save(new Order(OrderStatus.FAILED));
    if (true) {
      throw new RuntimeException();
    }
    return order;
  }

  public List<Order> findAll() {
    return orderRepository.findAll();
  }

}

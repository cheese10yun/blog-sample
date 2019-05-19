package yun.blog.transactionstudy.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderFailedService {

  private final OrderRepository orderRepository;


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Order fail() {
    final Order order = orderRepository.save(new Order(OrderStatus.FAILED));

    if (true) {
      throw new RuntimeException();
    }
    return order;
  }

}

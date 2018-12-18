package blog.yun.encapsulation;

import blog.yun.encapsulation.product.Message;
import blog.yun.encapsulation.product.MessageType;
import blog.yun.encapsulation.product.Order;
import blog.yun.encapsulation.product.OrderRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class Runner implements ApplicationRunner {

    private final OrderRepository orderRepository;

    public Runner(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        final Set<MessageType> types = new HashSet<>();
        types.add(MessageType.EMAIL);
        types.add(MessageType.KAKAO);
        types.add(MessageType.SMS);

        final Message message = Message.of(types);
        final Order order = Order.builder().message(message).build();

        orderRepository.save(order);
    }
}

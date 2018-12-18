package blog.yun.encapsulation;

import blog.yun.encapsulation.product.MessageType;
import blog.yun.encapsulation.product.Order;
import blog.yun.encapsulation.product.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void message_test() {

        final Order order = orderRepository.findById(1L).get();

        final List<MessageType> messages = order.getMessage().getTypes();

        assertThat(messages, hasItem(MessageType.EMAIL));
        assertThat(messages, hasItem(MessageType.KAKAO));
        assertThat(messages, hasItem(MessageType.KAKAO));
        assertThat(messages, hasItem(MessageType.SMS));

    }

}
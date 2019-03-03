package yun.blog.rabbitmqsample.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import yun.blog.rabbitmqsample.rabbitmq.RabbitMqEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class SignUpListener {

  private final AmqpTemplate amqpTemplate;

  @RabbitListener(queues = RabbitMqEvent.MEMBER_SIGNUPED_EVENT)
  public void handleSignUpEvent(final SignUpedEvent event) throws InterruptedException {
    log.error(event.toString());
    throw new IllegalArgumentException();
  }

}

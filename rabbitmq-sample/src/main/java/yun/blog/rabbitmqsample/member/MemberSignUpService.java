package yun.blog.rabbitmqsample.member;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yun.blog.rabbitmqsample.event.SignUpedEvent;
import yun.blog.rabbitmqsample.rabbitmq.RabbitMqEvent;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberSignUpService {

  private final MemberRepository memberRepository;
  private final AmqpTemplate amqpTemplate;


  public Member doSignUp(final SignUpRequest dto) {
    final Member member = memberRepository.save(dto.toEnttiy());

    amqpTemplate.convertAndSend(RabbitMqEvent.MEMBER_SIGNUPED_EVENT, SignUpedEvent.of(member));

    return member;
  }


}

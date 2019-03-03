```java
public interface RabbitMqEvent {
  String MEMBER_SIGNUPED_EVENT = "signuped.event";
}
```
* 이벤트 이름을 작성하는 객체입니다. 인터페이스로 관리하는게 효율적으로 보여짐


## Publisher : RabbitTemplate
```java
  @Bean
  public RabbitTemplate amqpTemplate() {
    RabbitTemplate rabbitTemplate = new RabbitTemplate();
    rabbitTemplate.setConnectionFactory(connectionFactory);
    rabbitTemplate.setMandatory(true);
    rabbitTemplate.setChannelTransacted(true);
    rabbitTemplate.setReplyTimeout(60000);
    rabbitTemplate.setMessageConverter(queueMessageConverter());
    return rabbitTemplate;
  }
```
* 이벤트를 Publisher하는 template를 생성합니다.
* ConnectionFactory 의존성 주입을 받아 생성합니다.
* message conveter를 등록시킵니다. 다른 예제들은 Bean으로 등록시는데 딱히 Bean으로 등록시킬 필요는 없어 보입니다.
* `setChannelTransacted` 플래극 값은 Transaction 으로 생성하게 해줍니다. (`@Transcational` 이 붙은 것처럼 처리됩니다. 만약 데이터베이스의 트랜잭션과 연결된다면 트랜잭션이 전위 된다고 이해했습니다.)
* `setReplyTimeout` amqpTemplate의 timeout을 지정합니다.



## Comsumer : SimpleRabbitListenerContainerFactory

> [spring-amqp Document](https://docs.spring.io/spring-amqp/api/org/springframework/amqp/rabbit/listener/AbstractMessageListenerContainer.html#setDefaultRequeueRejected-boolean-)을 참고했습니다. 제가 이해한 부분으로 설명하는 것이라 틀린 부분이 있을 수 있습니다.

```java
@Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory) {
    final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setDefaultRequeueRejected(false);
    factory.setMessageConverter(queueMessageConverter());
    factory.setChannelTransacted(true);
    factory.setAdviceChain(RetryInterceptorBuilder
        .stateless()
        .maxAttempts(MAX_TRY_COUNT)
        .recoverer(new RabbitMqExceptionHandler())
        .backOffOptions(INITIAL_INTERVAL, MULTIPLIER, MAX_INTERVAL)
        .build());
    return factory;
  }
```
* `setDefaultRequeueRejected` true일 경우 리서너에서 예외가 발생시에 다시 큐에 쌓이게 됩니다. 예외 상황을 해제하기 전까지 무한 반복할 거같아 기본 설정이 false이고 true 변경할 일이 거의 없어 보입니다.
* `setChannelTransacted` 위 설정과 동일합니다.
* `recoverer` 예외를 핸들링할 수 있는 객체를 지정해서 핸들링 할 수 있습니다. 에러 로그, 에러 알림 정도 설정하면 좋을거 같음
* `maxAttempts` 예외가 발생 했을 경우 몇번을 더 재시도 할 횟수를 지정합니다.
* `backOffOptions` 재시도 횟수에 대한 옵션을 지정합니다. `3000, 3, 10000` 인자값을 지정 했다면 3초 간격으로 3으로 곱해서 최대 10초 까지 재시도 하라는 것입니다.

```java
public Member doSignUp(final SignUpRequest dto) {
    final Member member = memberRepository.save(dto.toEnttiy());
    amqpTemplate.convertAndSend(RabbitMqEvent.MEMBER_SIGNUPED_EVENT, SignUpedEvent.of(member));
    return member;
  }
```
* `convertAndSend` 위에서 등록한 template 기반으로 이벤트를 발생시킵니다.
* 첫 번째 인자는 이벤트의 이름(스트링 벨류), 이벤트 처리시에 넘겨줄 객체를 바인딩 시킵니다.

```java
  @RabbitListener(queues = RabbitMqEvent.MEMBER_SIGNUPED_EVENT)
  public void handleSignUpEvent(final SignUpedEvent event) {
    log.error(event.toString());
    throw new IllegalArgumentException();
  }
```
* `@RabbitListener` 어노테이션으로 리슨할 이벤트 이름을 지정합니다.
* `convertAndSend`에서 넘겨준 객체 `SignUpedEvent`를 매게변수를 받습니다.
* `IllegalArgumentException();` 설정을 통해서 예외를 발생시킵니다. 위에서 설정한 3번의 일정한 반복이 진행되는지 확인 해 봅니다.

![](https://github.com/cheese10yun/TIL/raw/master/assets/rabbitmq-retry.png)


```
2019-03-03 20:43:57.459 ERROR 58190 --- [cTaskExecutor-1] y.b.rabbitmqsample.event.SignUpListener  : SignUpedEvent(id=1, email=asdasd@asdsad.com, name=asdasd)
2019-03-03 20:44:00.466 ERROR 58190 --- [cTaskExecutor-1] y.b.rabbitmqsample.event.SignUpListener  : SignUpedEvent(id=1, email=asdasd@asdsad.com, name=asdasd)
2019-03-03 20:44:09.470 ERROR 58190 --- [cTaskExecutor-1] y.b.rabbitmqsample.event.SignUpListener  : SignUpedEvent(id=1, email=asdasd@asdsad.com, name=asdasd)

...

 Retries exhausted for message (Body:'{"id":1,"email":"asdasd@asdsad.com","name":"asdasd"}' MessageProperties [headers={__TypeId__=yun.blog.rabbitmqsample.event.SignUpedEvent}, contentType=application/json, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=, receivedRoutingKey=signuped.event, deliveryTag=1, consumerTag=amq.ctag-XBOTLdkMvnhT6vypyy7HTQ, consumerQueue=signuped.event])
```

* 3번의 재시도가 시도됨
* 등록된 예외 핸들러 `RabbitMqExceptionHandler` 객체에서 로깅 출력
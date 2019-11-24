# 스프링 클라우드 스트림 아키텍처

### 4개의 컴포넌트

### 소스(Source)
서비스가 메시지를 발행할 준비가 되면 소스를 사용해 메시지를 발생 시킨다. 소스는 발행될 메시지를 표현하는 POJO를 전달받는 스프링 애너테이션 인터페이스다. 소스는 메시지를 받아 직렬화(기본 직렬화 설정은 JSON)하고 메시지를 채널로 발행한다.

### 채널(Channel)
채널은 메시지를 생산자와 소비자가 메세지를 발행하거나 소비한 후 메시지를 보관할 큐를 추상화한 것이다. 채널 이름은 한상 대기 큐 의 이름과 관련 있지만 코드에서는 큐 이름을 직접 사용하지 않고 채널 이름을 사용한다. 따라서 채널이 읽거나 쓰는 큐를 전환하려면 애플리케이션 코드가 아닌 구성 정보를 변경해야 한다.

### 바인더(Binder)
바인더는 스프링 클라우드 스트림 프레임워크의 일부인 스프링 코드로 특정 메시지 플랫폼과 통신한다. 스프링 클라우드 스프림의 바인더를 사용하면 메시지를 발행하고 소비하기 위해 플랫폼 마다 별도의 라이브러리와 API를 제공하지 않고도 메시징을 사용할 수 있다.

### 싱크(Sink)
스프링 클라우드 스트림에서 서비스는 싱크를 사용해 큐에서 메시지를 받는다. 싱크는 들어오는 메시지를 위해 채널을 수신 대기하고, 메시지를 다시 POJO로 역질렬화한다. 이 과정에서 스프링 서비스의 비지니스 로직이 메시지를 처리할 수 있다.


## Code 
```kotlin
@SpringBootApplication
@EnableBinding(Source::class)
class SteamApplication

fun main(args: Array<String>) {
    runApplication<SteamApplication>(*args)
}
```

* `@EnableBinding(Source::class)`: 스프링 클라우드 스트림에 애플레킹션을 메시지 브로커로 바인딩하라고 알린다.
* `Source::class`를 사용해 해당 서비스가 Source 클래스에 정의된 채널들을 이용해 메시지 브로커와 통시낳게 된다.
* 스프링 클라우드 스트림은 메시지 브로커와 통신할 수 있는 기본 채널이 있다.

 
 ```kotlin
@Component
class SimpleSourceBean(
        private val source: Source
) {

    fun publishOrgChange(action: String, orgId: String) {
        println("Sending Kafka message: $action, for Organization Id: $orgId")

        val model = OrganizationChangeModel(
                OrganizationChangeModel::class.jvmName,
                action, orgId
        )

        source
                .output()
                .send(MessageBuilder
                        .withPayload(model)
                        .build()
                )
    }
}
```
특정 메시지 토픽에 대한 모든 통신은 채널이라는 스프링 클라우드 스티림 구조로 밠애한다. 채널은 자바 닝터페이스로 표현되며, 이 코드에서 Source 인터페이스를 사용한다. `Source` 인터페이스는 스프링 클라우드에서 정의한 인터페이스로 `output()` 메서드는 `MessageChannel` 클래스 타입을 반환한다.  `MessageChannel`은 메시지 브로커에 메시지를 보내는 방법을 정의한다. 실제 메시지 발행은 `publishOrgChange()` 메시지에서 이루어진다. 에 메서드는 OrganizationChangeModel 이라는 POJO를 만든다.

* 액션(action): 이벤트를 발생 시키는 액션이다. 메시지에 액션을 포함시키면 메시지 소비자가 이벤트를 처리하는 데 더 많은 컨텍스트를 제공할 수 있다.
+ 조직 ID: 이벤트와 연관된 조직 ID다.
* 상관관계 ID: 이벤트를 발생시키는 서비스 호출에 대한 상관관계 ID다. 상관관계ID는 서비스들은 경유하는ㄴ 세미지 흐름을 추적하고 디버깅하는 데 도움이 많이 되므로 항상 이벤트에 포하시켜야한다.


이것이 메시지를 보내는 데 필요한 코드 전부다. 하지만 실제 메시지 브로커뿐만 아니라 특정 메시지에 큐에서도 조직 서비스를 바인딩하는 방법을 다루지 않아서 아직까지 이 모든 것이 다소 마술 처럼 느껴질 것이다. **실제로 이 모든 일은 구성 설정으로 이루어진다.** 이 구성 정보는 서비스의 `application.yml` 파일이나 스프링 클라우드 컨비그에서 해당 환경에 맞게 설정할 수 있다.

```yml
spring:
  application:
    name: organization-service

  cloud:
    stream: # stream.bindings는 서비스가 스프링 클라우드 스트림의 메시지 크로커에 발생해려는 구성의 시작점
      bindings:
        output: # output은 채널 이름
          destination:  orgChangeTopic # 메시지를 넣을 메시지 큐(또는 토픽) 이름이다
          content-type: application/json# 스프링 클라우드 스트림에 송수신할 메시지 타입 정보를 제공한다
        kafka: # cloud.bindings.kafka 프로퍼티는 해당 서비스가 메시지 버스로 카파를 사ㅛㅇ할 것이라고 스프링에 전달한다 (대안으로 Rabbmit MQ를 사용할 수 있다.)
          binder:
            zkNodes: localhost # zkNodes와 brokers 프로퍼티는 스프링 클라우드 스트림에 카프카와 주키퍼의 네이퉈크를 위치를 전달한다.
            brokers: localhost
```
`cloud.bindings.kafka` 구성 프로퍼티는 스프링 클루아드 스트림이 서비스를 카프카에 바인딩할도록 설정한다. 하위 프로퍼티는 카프카 메시지 브로커 및 카프카와 함께 실행되는 아파치 주키퍼 서비스의 네트워크를 주소를 스프링 클라우드 스트림에 설정한다.

```kotlin
@Service
class OrganizationService(
        private val simpleSourceBean: SimpleSourceBean
) {

    fun saveOrg() {
        val orgId = UUID.randomUUID().toString()
        simpleSourceBean.publishOrgChange("save", orgId)
    }
}
```
메시지를 발행한다 
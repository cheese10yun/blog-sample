# Spring Conig Client

[Spring Config Server 정리](https://cheese10yun.github.io/spring-config-server/)를 통해서 Config Server에 대해서 알아봤습니다. 이제는 Config Client를 알아보겠습니다. 

각 서비스 애플리케이션은 해당 애플리케이션이 구동시 Config Server에 자신의 Config의 설정 파일을 읽어 오며, **애플리케이션이 구동중에도 Config 설정을 바꾸고 애플리케이션 재시작 없이 해당 변경 내용을 반영할 수 있습니다.**

![](images/config-server1.png)


## Config Client 구성

```gradle
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-bus-kafka")
}
```
* config: Config Client 의존성
* bus-kafka: Kafka 기반으로 Config 변경을 브로드캐스트로 전달, `1. 애플리케이션 구동시`에서는 사용하지 않을 예정이니 주석 처리하고 `3. Config 설정 및 추가 수정 Push` 할 때 사용 예정

```yml
server:
    port: 8181
spring:
    application:
        name: order-service # (1)

    kafka:
        bootstrap-servers: localhost:29092 # (2)

    config:
        import: "optional:configserver:http://localhost:8888" # (3)

management:
    endpoints:
        web:
            exposure:
                include:
                    - "*" # (4)
                    # - "refresh" # 위 '*'  으로 전체를 공개 해서 주석
                    # - "bus-refresh" # 위 '*'  으로 전체를 공개 해서 주석
    endpoint:
        refresh:
            enabled: true
```

* (1): 애플리케이션 이름을 지정 `spring.application.name` 값을 기준으로 **Config Repositroy 저장소에 있는 Config 파일을 인식해서 가져오기 때문에 반드시 두 값을 일치 시켜야 합니다.**
  * `spring.application.name=order-service`의경우 `order-service-{evn}.yml`을 Config 파알로 인식함
* (2): 변경사항을 전파하는 카프카 주소 작성
* (3): Config Server주소 작성, `optional:configserver:{address}` 으로 작성
* (4): `actuator`의 속성을 `*`으로 전체 공개, **실제 운영 애플리케이션에서는 필요한 부분만 공개 해야합니다.**


## 애플리케이션 구동


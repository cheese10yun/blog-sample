# Elasticsearch APM : 분산 트랜잭션 추적


분산 환경에서는 한 요청이 여러 서비스들의 호출로 이루어집니다. 이런 경우 여러 서비스 사이의 트랜잭션, 로그의 모니터링과 요청에 대한 순차적인 연결이 중요합니다.


![](images/apm-2-2.png)

유저의 본인 정보와 본인이 주문한 목록을 조회하는 플로우 입니다. API Gateway -> User Service(유저 정보 조회) -> Order Service(주문 목록 조회)

이런 경우 분산 환경에서의 트랜잭션 추적은 상당히 어려운 부분이 있습니다. 위 예제는 2대의 서버 밖에 없지만 연결 서비스가 많아지면 그 복잡도는 더욱 증가 됩니다. 이런 경우 연결된 요청의 트랜잭션을 시각화하여 제공해주는 솔루션이 매우 유용하게 사용될 수 있습니다. Elasticsearch APM은 이러한 서비스를 제공 해주고 있습니다. Elasticsearch APM의 기초적인 설명 및 설정 방법은 [Elasticsearch APM 기본설정](https://cheese10yun.github.io/elk-apm-1/)을 참고 해주세요.


![](images/apm-2-1.png)

User Service(유저 정보 조회) -> Order Service(주문 목록 조회)의 분산 트랜잭션에 대한 정보를 Elasticsearch APM에서 제공 해주고 있습니다. user-service, order-service의 각각의 트랜잭션에 사항을 표시 해주고 있습니다.

![](images/apm-2-3.png)

User Service의 트랜잭션에 대한 내용이 있습니다.

![](images/apm-2-4.png)

Order Service의 트랜잭션에 대한 내용이 있으며 당연한 이야기겠지만 `transaction.id`가 서로 다르고 `trace.id`는 `94ca4184a27bf5fdf00149541cfd141f`으로 동일 한것을 확인할 수 있습니다. 

![](images/apm-2-5.png)

해당 값으로 전체의 분산 트랜 잭션의 로그 데이터를 타임라인으로 확인 할 수 있습니다.




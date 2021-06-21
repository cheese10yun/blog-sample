# JPA JPQL의 동작에 대한 정리


## todo
* [ ] JPQL은 영속성 DB 조회이후 영속성 컨텍스트에 있는 내용이면 벼린다.
* [ ] 단한한 셈플 예제 -> https://cheese10yun.github.io/jpa-persistent-context/
* [ ] 왜 이렇게 동작하는지, 이는 REPEATABLE READ와 관련이 있음
* [ ] REPEATABLE REA 개념적인 설명
* [ ] JPA는 애플리케이션에서 동일한 트랜잭션에 대해서 REPEATABLE READ를 지원하는 거임
* [ ] 동일하지 않은 트랜잭션이거나, 영속성 컨텍스트를 하지 않은 Projections에서는 문제가 발생
* [ ] MySQL 에서는 기본적으로 REPEATABLE 격리 레벨이기 때문에 이는 발생하지 않음
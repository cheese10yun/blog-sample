

# 테스트 시나리오

1. 동적으로 변경 확인
2. 새로 부팅시 config-server의 yml이 우선순위가 더 높음
3. 로컬 서버에 없는 경우는 config-server 기반으로 설정 가져옴
4. 로깅 레벨 변경?


# TODO
* [ ] Config 서버의 간단하 소개,
  * [ ] 중앙 집중
  * [ ] 동적 변경
  * [ ] 이미지
* [ ] Private Repo SSH 관리
* [ ] 동적 변경



# Spring Config Server

스프링 Config Server는 각 어플리케이션에서 관리하는 Config(application.ym.) 설정을 중앙 저장소를 통해서 효울적으로 Confg 설정들을 관리하게 해주는 서버입니다. 또한 다양한 Config 저장소 환경을 제공 해주고 있습니다.

* Git(Github)
* JDBC
* REDIS
* AWS S3

무엇보다도 스프링 Config Server는 기존 스프링 애플리케이션들과 쉽게 통신이 가능하며 특정 설정들을 서버가 구동중에 동적으로 변경할 수 있는 장점이 있으며 Github을 사옹하고 있다면 별다른 어려움 없이 Config Server를 도입할 수 있습니다.


## Spring Config Server 구성 하기
* [ ] 의존성 설명
* [ ] @EnableConfigServer
* [ ] yml 설정 관련등등
* [ ] Private Repo 환경 ID/Password, SSH
* [ ] bootstrap.yml 설정 config 서버정도 설정하는듯
* [ ] 애플리케이션 이름과 컨피스 서버의 config 이름과 동일 해야함
* [ ] searchPath의미가 있나??
* [ ] @RefreshScope, @ConfigurationProperties 도 자동으로 가능한듯?




> 참고로 bootstrap.yml 은 application.yml 보다 먼저 로드하기 때문에 config 서버의 정보를 consuming 하는 서버에서 설정 정보가 누락되어 application 이 실행되지 않는 것을 방지한다.


> Spring Cloud 2020에서는 부트스트랩이 작동하는 방식을 변경했으며 새로운 스타터인 spring-cloud-starter-bootstrap.


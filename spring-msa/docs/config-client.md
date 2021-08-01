# Spring Conig Client

[Spring Config Server 정리](https://cheese10yun.github.io/spring-config-server/)를 통해서 Config Server에 대해서 알아봤습니다. 이제는 Config Client를 알아보겠습니다. 

각 서비스 애플리케이션은 해당 애플리케이션이 구동시 Config Server에 자신의 Config의 설정 파일을 읽어 오며, **애플리케이션이 구동중에도 Config 설정을 바꾸고 애플리케이션 재시작 없이 해당 변경 내용을 반영할 수 있습니다.**

![](images/config-server1.png)


## Config Client 구성

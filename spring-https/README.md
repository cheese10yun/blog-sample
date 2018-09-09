# Spring Boot Https 적용하기
스프링 부트에서는 정말 간단하게 Https를 적용할 수 있습니다.

## 키스토어 생성
```
keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 4000

키 저장소 비밀번호 입력:
새 비밀번호 다시 입력:
이름과 성을 입력하십시오.
  [Unknown]:  test
조직 단위 이름을 입력하십시오.
  [Unknown]:  test
조직 이름을 입력하십시오.
  [Unknown]:  test
구/군/시 이름을 입력하십시오?
  [Unknown]:  test
시/도 이름을 입력하십시오.
  [Unknown]:  test
이 조직의 두 자리 국가 코드를 입력하십시오.
  [Unknown]:  01
CN=test, OU=test, O=test, L=test, ST=test, C=01이(가) 맞습니까?
  [아니오]:  y
```

## properties 설정

### YML
```yml
server:
  ssl:
    key-store: keystore.p12
    key-store-password: cheese
    keyStoreType: PKCS12
    keyAlias: tomcat
```


### Properties
```
server.ssl.key-store: keystore.p12
server.ssl.key-store-password: cheese
server.ssl.keyStoreType: PKCS12
server.ssl.keyAlias: tomcat
```

## https 요청
![스크린샷 2018-09-10 오전 2.01.38](https://github.com/cheese10yun/blog-sample/blob/master/spring-https/assets/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202018-09-10%20%EC%98%A4%EC%A0%84%202.01.38.png)

![스크린샷 2018-09-10 오전 2.01.42](https://github.com/cheese10yun/blog-sample/blob/master/spring-https/assets/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202018-09-10%20%EC%98%A4%EC%A0%84%202.01.42.png)

* [스프링 부트](https://www.inflearn.com/course/스프링부트 ) 참고
# Spring OOP 프로그래밍 예제(3)

[Spring OOP 프로그래밍 예제(2)](https://github.com/cheese10yun/blog-sample/tree/master/bankapi)의 부족했던 부분을 정리한 포스팅입니다.

## 요구사항
* 환율 정보를 조회할 수 있다.
* 환율 정보를 재공해주는 은행은 신한, 우리 은행이 있다.
* 앞으로도 파트너 은행들은 계속 추가된다.
* KRW, VND으로 받는 통화는 신한은행 환율정보를 이용해야한다.
    * USD -> KRW, USD -> VND 환율 정보는 신한은행 API 사용
* USD으로 받는 통화는 우리은행 환율정보를 이용해야한다.
    * KRW -> USD, VND -> USD 우리은행 API

## 요구사항 정리




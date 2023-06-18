# 실무에서 적용하는 테스트 코드 작성 방법과 노하우



# 효율적인 Mock Test

## 기존 가맹점 등록 Flow

## 신규 가맹점 등록 Flow

## HTTP Mock Server Test Code의 문제점

## 신규 가맹점 등록 @MockBean 기반 Test Code

* 특징 설명
* 문제점 설명

## @TestConfiguration 기반으로 Test Code 변경

* 특징 설명
* 문제점 설명

> “테스트를 쉽게 하기 위해, 운영 코드 설계를 변경하는 것이 옳은가?”

## fixtures 해결

## 정리

| 방식                 | 장점                                   | 단점                                        |
|--------------------|--------------------------------------|-------------------------------------------|
| Mock Server Test   | HTTP 통신을 실제 진행 하여 서비스 환경과 가장 근접한 테스트 | HTTP 통신 Mocking을 의존하는 모든 구간에 Mocking 필요   |
| @MockBean	         | HTTP Mocking에 비해 비교적 간단하게 Mocking 가능 | Application Context를 재사용 못해 테스트 빌드 속도 저하  |
| @TestConfiguration | Application Context 이슈 해결            | 멀티 모듈 환경에서 @TestConfiguration Bean 사용 어려움 |
| java-test-fixtures | 멀티 모듈에서 환경에서 사용 가능                   | 멀티 모듈이 아닌 경우 불필요                          |

## 전하고 싶은 메시지

# 테스트 코드로 부터 피드백 받기



# TestContaners

* 테스트에서 도커 컨테이너를 실행할 수 있는 라이브러리.
  * https://www.testcontainers.org/
  * 테스트 실행시 DB를 설정하거나 별도의 프로그램 또는 스크립트를 실행할 필요 없다.
  * 보다 Production에 가까운 테스트를 만들 수 있다.
  * 테스트가 느려진다.



## 의존성 추가

```gradle
dependencies {
    testImplementation("org.testcontainers:junit-jupiter:1.12.5")
    testImplementation("org.testcontainers:mysql:1.12.5")
}
```
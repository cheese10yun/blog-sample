# Spring Cloud Gateway

명칭 | 설명
---|---
라우트(Route) | 라우트는 목적지 URI, 조건자 목록과 필터의 목록을 식별하기 위한 고유 ID로 구성된다. 라우트는 모든 조건자가 충족됐을 때만 매칭된다
조건자(Predicates) | 각 요청을 처리하기 전에 실행되는 로직, 헤더와 입력돤값 등 다양한 HTTP 요청이 정의된 기준에 맞는지를 찾는다.
필터(Filters) | HTTP 요청 또는 나가는 HTTP 응답을 수정할 수 있게한다. 다운스트림 요청을 보내기전이나 후에 수정할 수 있다. 라우트 필터는 특정 라우트에 한정된다.


* [ ] sleuth
* [ ] gateway
* [ ] A-B test
* [ ] eureka
* [ ] ribbon
* [ ] openfeign
* [ ] weighthigh 



retries: 시도해야하는 재시도 횟수입니다.

statuses: 재 시도해야하는 HTTP 상태 코드로 org.springframework.http.HttpStatus.

methods: 재 시도해야하는 HTTP 메소드로 org.springframework.http.HttpMethod.

series:를 사용하여 표시되는 재 시도 할 일련의 상태 코드 org.springframework.http.HttpStatus.Series입니다.

exceptions: 재 시도해야하는 throw 된 예외 목록입니다.

backoff: 재 시도에 대해 구성된 지수 백 오프입니다. 이 재시의 백 오프 기간 이후에 수행되어 firstBackoff * (factor ^ n), n반복된다. maxBackoff가 구성된 경우 적용되는 최대 백 오프는로 제한됩니다 maxBackoff. 경우 basedOnPreviousValue사실, 백 오프는 byusing 계산됩니다 prevBackoff * factor.

Retry활성화 된 경우 필터에 대해 다음 기본값이 구성 됩니다.

retries: 세 번

series: 5XX 시리즈

methods: GET 메서드

exceptions: IOException및TimeoutException

backoff: 비활성화 됨
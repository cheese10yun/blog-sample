# Spring ItemReader 성능 측정

Spring Batch에서 지원하는 대표 ItemReader, 커스텀해서 마든 ItemReader의 속도를 측정 해보고 정리 해보겠습니다.  

## 대상 ItemReader
* JPA Paging Reader
* Hibernate Cursor Reader
* JPA Cursor Reader
* No Offset 방식, [기억보단 기록을 - Spring Batch와 QuerydslItemReader](https://jojoldu.tistory.com/473) 참고
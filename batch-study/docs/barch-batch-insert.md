Batch Insert 성능 개선기

1. JPA에서 Batch Insert가 동작하지 않은 이유 길게 설명
2. JDBC Template를 이용한 Batch Insert 방식
    - 성능 측정
3. 문자열 기반의 문제 해결
4. 대안 솔루션
    - Mybatis, Data JDBC, Exposed
    - Exposed 결정 이유
5. Exposed 성능 측정

## 대량 등록을 위한 Insert

## JPA 에서는 왜 Batch Insert를 못할까 ?

## 그렇다면 대안은 ?

* 

## 성능 측정

### chunk size = 1,000
rows | JPA | JDBC | EXPOSED
-----|-----|------|--------
10,000 | 14577 | 567 | 942
50,000 | 58702 | 3060 | 3465
100,000 | 133004 | 3977 | 4650
500,000 | 628603 | 16156 | 20605
1,000,000 | 1344047 | 32339 | 39008
5,000,000 | 6783322 | 152429 | 207833
10,000,000 | 13565421 | 298893 | 383992

### chunk size = 5,000
rows  | JDBC | EXPOSED
-----|------|--------
10,000  | 517 | 779
50,000  | 2416 | 2975
100,000 | 2779 | 4777
500,000 | 10973 | 20611
1,000,000 | 21401 | 34026
5,000,000 | 110473 | 162615
10,000,000 | 211603 | 413898

### chunk size = 10,000
rows  | JDBC | EXPOSED
-----|------|--------
10,000  | 372 | 769
50,000  | 1705 | 3568
100,000 | 2414 | 4177
500,000 | 11292 | 15984
1,000,000 | 21963 | 30768
5,000,000 | 103363 | 154183
10,000,000 | 197361 | 337296


### chunk size = 10,000
rows  | JDBC | EXPOSED
-----|------|--------
10,000  | 372 | 769
50,000  | 1705 | 3568
100,000 | 2414 | 4177
500,000 | 11292 | 15984
1,000,000 | 21963 | 30768
5,000,000 | 103363 | 157819
10,000,000 | 197361 | 337296

# 참고
* [Spring Data에서 Batch Insert 최적화](https://homoefficio.github.io/2020/01/25/Spring-Data%EC%97%90%EC%84%9C-Batch-Insert-%EC%B5%9C%EC%A0%81%ED%99%94/)
* [JPA GenerationType에 따른 INSERT 성능 차이](https://github.com/HomoEfficio/dev-tips/blob/master/JPA-GenerationType-%EB%B3%84-INSERT-%EC%84%B1%EB%8A%A5-%EB%B9%84%EA%B5%90.md)
* [JPA Batch inserts Document](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#batch-session-batch-insert)
* [How do persist and merge work in JPA](https://vladmihalcea.com/jpa-persist-and-merge/)
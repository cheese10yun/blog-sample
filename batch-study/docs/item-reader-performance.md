# Spring Batch Reader 성능 분석

* [ ] JpaPaging Reader
* [ ] Zero Offset Reader
* [ ] Cursor Reader
* [ ] JPA 영속성 컨텍스트 초가화 시점은 ?


## 공부할 내용
* 커서 리더는 ResultSet를 사용하는데 ResultSet 처리 방식은 ?
* JpaPaging Reader의 다양한 속성들이 있는데 그것들이 무엇을 의미하는지?
* JPA 엔티티 vs 프로젝션 성능 차이는? 그에 따른 영속성 컨텍스트 비용은 ?




## jpaPagingItemReader


Reader      rows      Chunk Size      소요 시간(ms)
JpaPagingItemReader             10,000      1000      633      
JpaPagingItemReader             50,000      1000      3827      
JpaPagingItemReader             100,000      1000      11741      
JpaPagingItemReader             500,000      1000      206747     
JpaPagingItemReader             1,000_000      1000      861599
JpaPagingItemReader             5,000_000      1000

QueryDslNoOffsetPagingReader             10,000      1000      658      
QueryDslNoOffsetPagingReader             50,000      1000      2004      
QueryDslNoOffsetPagingReader             100,000      1000      3523      
QueryDslNoOffsetPagingReader             500,000      1000      15501     
QueryDslNoOffsetPagingReader             1,000_000      1000      28732
QueryDslNoOffsetPagingReader             5,000_000      1000     165249


JpaCursorItemReader             10,000      1000      448      
JpaCursorItemReader             50,000      1000      1605      
JpaCursorItemReader             100,000      1000      2886      
JpaCursorItemReader             500,000      1000      17411      
JpaCursorItemReader             1,000,000      1000      25439      
JpaCursorItemReader             5,000,000      1000     132552


## 참고


## log

```
```


```
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?
2021-05-23 20:47:10.148  INFO 51252 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?, ?
2021-05-23 20:47:10.188  INFO 51252 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000


```


QueryDslNoOffsetPagingReader
```
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? and payment0_.id>=? and payment0_.id<=? order by payment0_.id asc limit ?
2021-05-24 02:05:18.123  INFO 7794 --- [           main] uration$$EnhancerBySpringCGLIB$$e3daa2bc : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? and payment0_.id>? and payment0_.id<=? order by payment0_.id asc limit ?
2021-05-24 02:05:18.167  INFO 7794 --- [           main] uration$$EnhancerBySpringCGLIB$$e3daa2bc : item size 1000

~~

Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? and payment0_.id>? and payment0_.id<=? order by payment0_.id asc limit ?
2021-05-24 02:07:48.463  INFO 7794 --- [           main] uration$$EnhancerBySpringCGLIB$$e3daa2bc : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? and payment0_.id>? and payment0_.id<=? order by payment0_.id asc limit ?
2021-05-24 02:07:48.491  INFO 7794 --- [           main] uration$$EnhancerBySpringCGLIB$$e3daa2bc : item size 1000

```



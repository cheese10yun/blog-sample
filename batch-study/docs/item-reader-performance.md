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
JpaPagingItemReader             10,000      1000      513      
JpaPagingItemReader             50,000      1000      1763      
JpaPagingItemReader             100,000      1000      4343      
JpaPagingItemReader             500,000      1000      40127     
JpaPagingItemReader             1,000_000      1000      123889
JpaPagingItemReader             5,000_000      1000      


JpaCursorItemReader             10,000      1000      448      
JpaCursorItemReader             50,000      1000      1239      
JpaCursorItemReader             100,000      1000      2216      
JpaCursorItemReader             500,000      1000      9766      
JpaCursorItemReader             1,000,000      1000      19623      
JpaCursorItemReader             5,000,000      1000            



## 참고


## log

```
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?
2021-05-23 20:39:41.022  INFO 50035 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?, ?
2021-05-23 20:39:41.051  INFO 50035 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000

051 - 022 = 29

Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?, ?
2021-05-23 20:41:44.281  INFO 50035 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?, ?
2021-05-23 20:41:44.492  INFO 50035 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?, ?
```


```
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?
2021-05-23 20:47:10.148  INFO 51252 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.order_id as order_id3_0_ from payment payment0_ limit ?, ?
2021-05-23 20:47:10.188  INFO 51252 --- [           main] uration$$EnhancerBySpringCGLIB$$4d92f8c5 : item size 1000



```






----------------

## 참고

## 공부할 내용
* 커서 리더는 ResultSet를 사용하는데 ResultSet 처리 방식은 ?
* JpaPaging Reader의 다양한 속성들이 있는데 그것들이 무엇을 의미하는지?
* JPA 엔티티 vs 프로젝션 성능 차이는? 그에 따른 영속성 컨텍스트 비용은 ?

rows	JpaPagingItemReader     QueryDslNoOffsetPagingReader        JpaCursorItemReader
10,000      778     658       448
50,000      3243        2004       1605
100,000     8912        3523       2886
500,000     205469      15501       17411
1,000_000       1048979     28732       25439
5,000_000       ?       165249       132552



Reader      rows      Chunk Size      소요 시간(ms)
JpaPagingItemReader             10,000      1000      778      
JpaPagingItemReader             50,000      1000      3243      
JpaPagingItemReader             100,000      1000      8912      
JpaPagingItemReader             500,000      1000      205469     
JpaPagingItemReader             1,000_000      1000      1048979
JpaPagingItemReader             5,000_000      1000     ?
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


## log

```
```


```
# rows 1_000_000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?
2021-05-25 22:40:25.963  INFO 93165 --- [           main] uration$$EnhancerBySpringCGLIB$$d8232fb2 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?, ?
2021-05-25 22:40:26.016  INFO 93165 --- [           main] uration$$EnhancerBySpringCGLIB$$d8232fb2 : item size 1000

# rows 1_000_000

# rows 5_000_000

Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?
2021-05-25 23:02:09.623  INFO 96711 --- [           main] uration$$EnhancerBySpringCGLIB$$cea25d47 : item size 1000
Hibernate: select payment0_.id as id1_0_, payment0_.amount as amount2_0_, payment0_.created_at as created_3_0_, payment0_.order_id as order_id4_0_, payment0_.updated_at as updated_5_0_ from payment payment0_ where payment0_.created_at>=? order by payment0_.created_at DESC limit ?, ?
2021-05-25 23:02:09.672  INFO 96711 --- [           main] uration$$EnhancerBySpringCGLIB$$cea25d47 : item size 1000



# rows 5_000_000

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



#############


```sql
CREATE TABLE `payment`
(
    `id`         bigint(20)     NOT NULL AUTO_INCREMENT,
    `amount`     decimal(19, 2) NOT NULL,
    `created_at` datetime       NOT NULL,
    `order_id`   bigint(20)     NOT NULL,
    `updated_at` datetime       NOT NULL,
    PRIMARY KEY (`id`),
    KEY `IDXfxl3u00ue9kdoqelvslc1tj6h` (`created_at`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
```

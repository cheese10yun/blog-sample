> [Spring Batch와 QuerydslItemReader](https://github.com/jojoldu/spring-batch-querydsl/blob/master/posts/querydsl-reader/README.md)을 보고 정리한 글입니다.

![](https://github.com/jojoldu/spring-batch-querydsl/raw/master/posts/querydsl-reader/images/chunk.png)
> 이미지 출처 [Spring Batch와 QuerydslItemReader](https://github.com/jojoldu/spring-batch-querydsl/tree/master/posts/querydsl-reader)

* `doReadPage()`
  * `page`, `offset`와 `pageSize`을 이용해 데이터를 가져옵니다.
* `read()`
  * `doReadPage()`로 가져온 데이터들을 하나씩 processor로 전달합니다.
  * 만약 `doReadPage()`로 가져온 데이터를 모두 processor에 전달했다면, 다음 페이지 데이터를 가져오도록 `doReadPage()`를 호추합니다.

여기서 JPQL이 실행되는 부분은 `doReadPage()` 입니다. 죽 `doReadPage()` 에서 쿼리가 수행되는 부분을 Quety DSL의 쿼리로 변경하면 됩니다.


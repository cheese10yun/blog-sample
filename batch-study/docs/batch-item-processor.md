> [Spring Batch 가이드 - 9. Spring Batch 가이드 - ItemProcessor](https://jojoldu.tistory.com/347)을 보고 정리한 글입니다.

# Item Processor

`ItemProcessor`는 데이터 가공을 담당하는 역할을 합니다. 데이터 가공은 Writer에서도 충분히 구현 가능하지만 객체지향의 핵심인 책임과 역할을 중심으로 보면 Processor, Writer를 분리하는 것이 좋습니다.

![](https://camo.githubusercontent.com/c5dd8fb6b96a268a1b2dd8cc8f985a35a27d0b7b/68747470733a2f2f646f63732e737072696e672e696f2f737072696e672d62617463682f646f63732f342e302e782f7265666572656e63652f68746d6c2f696d616765732f6368756e6b2d6f7269656e7465642d70726f63657373696e672e706e67)
> [이미지 출처 docs.spring.io](https://docs.spring.io/spring-batch/docs/4.0.x/reference/html/index-single.html#chunkOrientedProcessing)

ItemProcessor로 크게 2가지 처리를 합니다.

* 반환 
  * Reader에서 읽은 데이터를 원하는 데이터 타입으로 변환해서 Writer에게 넘긴다.
* 필터
  * Reader에서 넘겨준 데이터를 Writer로 넘겨 줄것인지를 결정
  * **null을 반환하면 Writer에 전달되지 않습니다.**

## 기본 사용법
```java
public interface ItemProcessor<I, O> {

  O process(I item) throws Exception;

}
```
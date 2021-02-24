# Batch Insert 성능 향상기 2편 - 성능 측정

## JPA Batch Insert

JPA + MySQL + GenerationType.IDENTITY 조합으로는 Batch Insert를 사용할 수 없습니다. 자세한 내용은 [Batch Insert 성능 향상기 1편 - With JPA](https://cheese10yun.github.io/jpa-batch-insert/)에서 자세하게 정리 했습니다. 

## 다른 솔루션 찾기
다양한 솔루션이 있지만 결론 부터 말씀드리면 [Exposed](https://github.com/JetBrains/Exposed)라는 도구를 선택했습니다. 해당 솔루션을 찾기 위한 요구사항은 다음과 같았습니다. 

### 솔루션에 대한 요구사항
* SQL 관련 작업들을 문자열로 처리하지 하지 않고 DSL 표현하며 DSL 표현이 풍부할것
* Batch Insert 외에도 조회 작업 등에도 사용이 용이할것
* JDBC `addBatch()` 직접 호출하는 코드와 성능 적인 차이가 거의 없을것


## Insert 성능 측정

Insert에 대한 성능 측정은 JPA, JDBC, Exposed 3가지 솔루션으로 진행하겠습니다. JPA는 단건으로 저장하며, JDBC는 문자열 기반으로 `addBatch()`를 직접 호출, Exposed는 자체적으로 지원하는 Batch Insert를 진행할 예정입니다.


### 성능 측정 플로우
Spring Batch 기반으로 `payment` 테이블를 N개 읽어서 `payment_back` 테이블로 저장하는 플로우 입니다.


### Batch Code


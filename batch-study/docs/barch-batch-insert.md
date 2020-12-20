Batch Insert 성능 개선기

1. JPA에서 Batch Insert가 동작하지 않은 이유 길게 설명
2. JDBC Template를 이용한 Batch Insert 방식
   - 성능 측정
3. 문자열 기반의 문제 해결
4. 대안 솔루션
    - Mybatis, Data JDBC, Exposed
    - Exposed 결정 이유
5. Exposed 성능 측정




[jpaInsertStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 265149
시작일시: 2020-12-20T00:31:35.178+09:00[Asia/Seoul]
종료일시: 2020-12-20T00:36:00.327+09:00[Asia/Seoul]
읽기갯수: 100000
필터갯수: 0
쓰기갯수: 100000
커밋갯수: 101


[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 172103
시작일시: 2020-12-20T00:40:59.498+09:00[Asia/Seoul]
종료일시: 2020-12-20T00:43:51.601+09:00[Asia/Seoul]
읽기갯수: 100000
필터갯수: 0
쓰기갯수: 100000
커밋갯수: 101

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 175111
시작일시: 2020-12-20T00:47:22.538+09:00[Asia/Seoul]
종료일시: 2020-12-20T00:50:17.649+09:00[Asia/Seoul]
읽기갯수: 100000
필터갯수: 0
쓰기갯수: 100000
커밋갯수: 21

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 163425
시작일시: 2020-12-20T02:42:58.217+09:00[Asia/Seoul]
종료일시: 2020-12-20T02:45:41.642+09:00[Asia/Seoul]
읽기갯수: 100000
필터갯수: 0
쓰기갯수: 100000
커밋갯수: 101

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 203312
시작일시: 2020-12-20T03:21:21.472+09:00[Asia/Seoul]
종료일시: 2020-12-20T03:24:44.784+09:00[Asia/Seoul]
읽기갯수: 100000
필터갯수: 0
쓰기갯수: 100000
커밋갯수: 1001

==============

### reactor 12 thread
[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 166837
시작일시: 2020-12-20T03:26:58.804+09:00[Asia/Seoul]
종료일시: 2020-12-20T03:29:45.641+09:00[Asia/Seoul]
읽기갯수: 100000
필터갯수: 0
쓰기갯수: 100000
커밋갯수: 101

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 1252528
시작일시: 2020-12-20T03:45:07.072+09:00[Asia/Seoul]
종료일시: 2020-12-20T04:05:59.6+09:00[Asia/Seoul]
읽기갯수: 300000
필터갯수: 0
쓰기갯수: 300000
커밋갯수: 301

### 1 thread
[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 169938
시작일시: 2020-12-20T03:32:22.787+09:00[Asia/Seoul]
종료일시: 2020-12-20T03:35:12.725+09:00[Asia/Seoul]
읽기갯수: 100000
필터갯수: 0
쓰기갯수: 100000
커밋갯수: 101

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 1297081
시작일시: 2020-12-20T11:31:50.729+09:00[Asia/Seoul]
종료일시: 2020-12-20T11:53:27.81+09:00[Asia/Seoul]
읽기갯수: 300000
필터갯수: 0
쓰기갯수: 300000
커밋갯수: 301

### rx 12 thread

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 1271588
시작일시: 2020-12-20T12:04:18.541+09:00[Asia/Seoul]
종료일시: 2020-12-20T12:25:30.129+09:00[Asia/Seoul]
읽기갯수: 300000
필터갯수: 0
쓰기갯수: 300000
커밋갯수: 301

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 114789
시작일시: 2020-12-20T12:30:31.421+09:00[Asia/Seoul]
종료일시: 2020-12-20T12:32:26.21+09:00[Asia/Seoul]
읽기갯수: 80000
필터갯수: 0
쓰기갯수: 80000
커밋갯수: 81

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 113059
시작일시: 2020-12-20T12:40:10.818+09:00[Asia/Seoul]
종료일시: 2020-12-20T12:42:03.877+09:00[Asia/Seoul]
읽기갯수: 80000
필터갯수: 0
쓰기갯수: 80000
커밋갯수: 81

[batchInsertExposedStep 리포트]
최종상태: exitCode=COMPLETED;exitDescription=
소요시간: 123094
시작일시: 2020-12-20T19:37:13.81+09:00[Asia/Seoul]
종료일시: 2020-12-20T19:39:16.904+09:00[Asia/Seoul]
읽기갯수: 80000
필터갯수: 0
쓰기갯수: 80000
커밋갯수: 81
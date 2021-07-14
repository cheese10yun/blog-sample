# Spring Batch With Kotlin

스프링 배치에 대한 예제를 코틀린 기반으로 연습해보는 Repo 입니다.

* [Multi Step](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/multi-step.md)
* [Batch Table](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-table.md)
* [Batch Test](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-test.md)
* [Batch Job](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-job.md)
* [Batch Batch Scope & Job Parameter](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-scope-job-parameter.md)
* [Batch Chunk](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-chunk.md)
* [Batch ItemReader](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-item-reader.md)
* [Batch Writer](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-writer.md)
* [Batch Process](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-item-processor.md)
* [Batch CustomItemReader](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-custom-item-reader.md)
* [Batch CSV Writer](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-csv-wirter.md)
* [Batch CSV Reader](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-csv-reader.md)
* [Batch 개발](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-application.md)
* [Batch Test 노하우](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-test-2.md)
* [Batch Insert 성능 향상기 1편 - JPA Batch Insert](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/jpa-batch-insert.md)
* [Batch Insert 성능 향상기 2편 - 성능 측정](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/batch-batch-insert.md)
* [Batch Reader 성능 측정 및 분석](https://github.com/cheese10yun/blog-sample/blob/master/batch-study/docs/item-reader-performance.md)

## Todo
- [ ] 대표 ItemReader 분석 해보기 (PagingItemReade, CursorItemReader)
- [ ] Reactor 기반으로 Writer 성능 향상 시키기



-javaagent:/Users/yun.cheese/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.24.0.jar -Delastic.apm.service_name=spring-boot-api -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.example.api 


-javaagent:/Users/yun.cheese/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.24.0.jar -Delastic.apm.service_name=batch-bulk-insert -Delastic.apm.server_url=http://124.80.103.104:8200 -Delastic.apm.application_packages=com.batch.task

-javaagent:/Users/yun.cheese/yun/blog-sample/elk-sample/api/elastic-apm-agent-1.24.1.jar -Delastic.apm.service_name=batch-bulk-insert -Delastic.apm.server_url=http://192.168.0.10:8200 -Delastic.apm.application_packages=com.example.api -Delastic.apm.verify_server_cert=false


422877



| ELK Agent 유무 | rows(읽기/쓰기) | Job 시간(ms) |
| -------------- | --------------- | ------------ |
| x              | 100,000         | 4121         |
| O              | 100,000         | 4294         |
| x              | 250,000         | 9102         |
| O              | 250,000         | 9591         |
| x              | 500,000         | 18969        |
| O              | 500,000         | 20293        |
| x              | 1,000,000       | 35008        |
| O              | 1,000,000       | 35717        |



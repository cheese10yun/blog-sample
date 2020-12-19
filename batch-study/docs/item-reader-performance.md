# ??

```
2020-02-19 01:40:02.653  INFO 83668 --- [           main] o.s.batch.core.step.AbstractStep         : Step: [step] executed in 5m8s605ms
2020-02-19 01:40:02.726  INFO 83668 --- [           main] o.s.b.c.l.support.SimpleJobLauncher      : Job: [SimpleJob: [name=querydslZeroOffsetItemReaderJob]] completed with the following parameters: [{run.id=9, -job.name=querydslZeroOffsetItemReaderJob}] and the following status: [COMPLETED] in 5m8s734ms
2020-02-19 01:40:02.732  WARN 83668 --- [extShutdownHook] o.s.b.f.support.DisposableBeanAdapter    : Destroy method 'close' on bean with name 'reader' threw an exception: org.springframework.batch.item.ItemStreamException: Error while closing item reader
```
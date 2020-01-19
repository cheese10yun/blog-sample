> [Spring Batch 가이드 - 7. ItemReader](https://github.com/jojoldu/spring-batch-in-action/blob/master/6_CHUNK.md)을 보고 정리한 글입니다.

# ItemReader
기존에 Step에서는 Tasklet 단위로 처리되고, Tasklet 중에서 ChunkOrientedTasklet를 통해 Chunk를 처리하며 이를 구성하는 3요소로 ItemReader, ItemWProcessor, ItemWriter가 있었습니다.

## ItemReader 소개
![](https://github.com/cheese10yun/TIL/blob/master/assets/spring-batch-item-reader.png?raw=true)


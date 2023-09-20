# Redis 강의 정리


## 데이터 타입 알아 보기

### Strings

* Strings 문자열, 숫자, serialized object (JSON string)등 저장


```redis

set lecture inflean-redis

mset price 100 language ko

mget lecture price language;

incr price

incrby price 9

set inflean-redis '{"price": 100, "language": "ko"}'

get inflean-redis

set inflean-redis:ko:price 200

get inflean-redis:ko:price
```

### Lists

* Lists string을 Linked List로 저장 -> push / pop에 최적화 O(1), Queue(FIFO), Stack(FILO)

명령어




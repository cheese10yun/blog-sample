# Redis 소개와 특징

# Redis Data Type 이해

## String 요약

* 가장 기본적인 데이터 타입 제일 많이 사용됨
* 바이트 배열 저장
* 바이너리로 변환할 수 있는 모든 데이터를 저장 가능
* 최대 크기는 512MB

### 주요 명령어


| 명령어 | 기능                                            | 에졔                        |
| ------ | ----------------------------------------------- | --------------------------- |
| SET    | 특정 키에 문자열 값을 저장한다.                 | SET say hello               |
| GET    | 특정 키의 문자열 값을 얻어 온다.                | GET say                     |
| INCR   | 특정 키의 값을 Integer로 취급하여 1 증가시킨다. | INCR mycount                |
| DECR   | 특정 키의 값을 Integer로 취급하여 1 감소시킨다. | DECR mycount                |
| MSET   | 여러 키에 대한 값을 한번에 저장한다.            | MSET mine milk yours coffee |
| MGET   | 여러 키에 대한 값을 한번에 얻어 온다.           | MSET mine yours             |

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

```
set key1 1
get key1

set mycount 10
incr mycount
decr mycount
get mycount

mset key1 hi key2 hello
mget key1 key2
```

</div>
</details>



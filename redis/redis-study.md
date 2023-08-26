# Redis 소개와 특징

# Redis Data Type 이해

## Strings

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

</details>

## Lists

* Linked-list 형태의 자료구조(인덱스 접근은 느리지만 데이터 추가/삭제가 빠름)
* Queue와 Stack으로 사용할 수 있음


| 명령어 | 기능                                               | 에졔                |
| ------ | -------------------------------------------------- | ------------------- |
| LPUSH  | 리스트의 왼쪽(head)에 새로운 값을 추가한다.        | LPUSH mylist apple  |
| RPUSH  | 리스트의 오른쪽(tail)에 새로운 값을 추가한다.      | RPUSH mylist banana |
| LLEN   | 리스트에 들어있는 아이템 개수를 반환한다.          | LLEN mylist         |
| LRANGE | 리스트의 특정 범위를 반환한다.                     | LRANGE mysql 0 -1   |
| LPOP   | 리스트의 왼쪽(head)에서 값을 삭제하고 반환한다.    | LPOP mylist         |
| RPOP   | 리스트이 오른쪽(tail)에서 값을 삭제 하고 변환한다. | RPOP mylist         |

<details>
<summary>접기/펼치기</summary>

```
LPUSH mylist apple
LPUSH mylist banana

LLEN mylist

LRANGE mylist 0 -1
LRANGE mylist 0 -2

LPOP mylist
RPOP mylist
```

</details>

## Sets

* 순서가 없는 유니크한 값의 집합
* 검색이 빠름
* 개별 접근을 위한 인덱스가 존재하지 않고, 집합 연산이 가능(교집합, 합집합)


| 명령어    | 기능                                        | 예제                  |
| --------- | ------------------------------------------- | --------------------- |
| SADD      | Set에 데이터를 추가한다.                    | SADD myset apple      |
| SREM      | Set에 데이터를 삭제한다.                    | SRM myset apple       |
| SCARD     | Set에 저장된 아이템 개수를 반환한다.        | SCARD myset           |
| SMEMBERS  | Set에 저장된 아이템을 반환한다.             | SMEMBERS myset        |
| SISMEMBER | 특정 값이 Set에 포함되어 있는지를 반환한다. | SISMEMBER myset apple |

<details>
<summary>접기/펼치기</summary>

```
SADD myset apple
SADD myset banana

SCARD myset
SMEMBERS myset

SISMEMBER myset apple
SISMEMBER myset grape
```

</details>

## Hashes

* 하나의 key 하위에 여러개의 field-value 쌍을 저장
* 여러 필드를 가진 객체를 저장하는 것으로 생각할 수 있음
* HINCRBY 명령어를 사용해 카운터로 활용 가능


| 명령어  | 기능                                                         | 예제                       |
| ------- | ------------------------------------------------------------ | -------------------------- |
| HSET    | 한개 또는 다수의 필드에 값을 저장한다.                       | HSET user name bear age 10 |
| HGET    | 특정 필드의 값을 반환한다.                                   | HGET user1 name            |
| HMGET   | 한개 이상의 필드 값을 반환한다.                              | HMGET user1 name age       |
| HINCRBY | 특정 필드 값을 Integer로 취급하여 저장한 숫자를 증가 시킨다. | HINCRBY user1 viewcount 1  |
| HDEL    | 한개 이상의 필드를 삭제한다.                                 | HDEL user1 name age        |

<details>
<summary>접기/펼치기</summary>

```
HSET user name bear age 10
HGET user name

HMGET user name age

HSET user viewcount 15

HGET user viewcount

HINCRBY user viewcount 3

HKEYS user

HDEL user name age

HKEYS user
```

</details>

## SortedSets

* Set과 유사하게 유니크한 값의 집합
* 각 값은 연관된 score를 가지고 정렬되어 있음
* 정렬된 상태이기에 빠르게 최소/최대값을 구할 수 있음
* 순위 계산, 리더보드 구현 등에 활용


| 명령어   | 기능 제                                               | 예제                           |
| -------- | ----------------------------------------------------- | ------------------------------ |
| ZADD     | 한개 또는 다수의 값을 추가 또는 업데이트 한다.        | ZADD myrank 10 apple 20 banana |
| ZRANGE   | 특정 범위의 값을 반환한다.(오름차순으로 정렬된 기준)  | ZRANGE myrank 0 1              |
| ZRANK    | 특정 값의 위치(순위)를 반환한다.(오름차순으로 정렬된) | ZRANK myrank apple             |
| ZREVRANK | 특정 값의 위치(순위)를 반환한다.(내림차순으로 정렬된) | ZREVRANK myrank apple          |
| ZREM     | 한개 이상의 값을 삭제한다.                            | ZREM myrank apple              |

<details>
<summary>접기/펼치기</summary>

```
ZADD myrank 10 apple 20 banana 30 grape

ZRANGE myrank 0 1

ZREVRANK myrank apple

ZRANK myrank apple
```

</details>

# 게임 리더 보드 만들기

## 리더보드의 특성과 기능 요구사항

* 게임이나 경쟁에서 상위 참가자의 랭킹과 점수를 보여주는 기능
* 순위로 나타낼 수 있는 다양한 대상에 응용

### 리더보드의 동작 API

* 점수 생성/업데이트 -> SetScore(userid, score)
* 상위 랭킹 조회 -> getRange(1~10)
* 특정 대상 순위 조회 -> getrank(userId)

### RDBMS 사용시 문제

* 한 행에만 접근하므로 비교적 빠름
* 랭킹 범위나 특정 대상의 순위 조회
  * 데이터 정렬하거나 count등의 집계 함수연산을 수행하므로 데이터가 많아질수록 속도가 느려짐

### Redis 사용시 장점

* 순위 데이터에 적합한 sorted-set 자료구조를 사용하면 socre를 통해 자동을 정렬됨
* 용도에 특회된 오퍼레이션이 존재하므로 사용이 간단함
* 자료구조의 특성으로 데이터 조회가 빠름
* 빈번한 액세스에 유리한 In-memory DB의 속도

# Pub/Sub을 이용해 손쉽게 채팅방 기능 구현하기

## Pub/Sub 패턴

* 메시징 모델 중 하나로 발행과 구독 역할로 개념화 한 형태
* 발생자와 구독자는 서로에 대한 정보 없이 특정 토픽 or 채널을 매개로 송수신

## 메시지미들웨어 사용의 장점

* 비동기: 통신의 비동기 처리
* 낮은 결합도: 송신자와 수신자 직접 서로 의존하지 않고 공통 미들웨어에 의존
* 탄력성: 구성원들간에 느슨한 연결로 인해 일부 장애가 생겨도 영향이 최소화됨

## Redis의 Pub/Sub 특징

* 메시지가 큐에 저장되지 않음
* Kafka의 컨슈머의 그룹같은 분산처리 개념이 없음
* 메시지 발행 시 push 방식으로 subscriber들에게 전송
* subscriber가 늘어날수록 성능저하

## Redis의 Pub/Sub의 유즈케이스


* 실시간으로 빠르게 전송되야 하는 메시지
* 메시지 유실을 감내할 수 있는 케이스
* 최대 1회 전송 패턴이 적합한 경우
* Subscriber들이 다양한 채널을 유동적으로 바꾸면서 한시적으로 구독하는 경우

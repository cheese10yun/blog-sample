# Redis 소개와 특징

# Redis Data Type 이해

## Strings

* 가장 기본적인 데이터 타입 제일 많이 사용됨
* 바이트 배열 저장
* 바이너리로 변환할 수 있는 모든 데이터를 저장 가능
* 최대 크기는 512MB

### 주요 명령어

| 명령어  | 기능                              | 에졔                          |
|------|---------------------------------|-----------------------------|
| SET  | 특정 키에 문자열 값을 저장한다.              | SET say hello               |
| GET  | 특정 키의 문자열 값을 얻어 온다.             | GET say                     |
| INCR | 특정 키의 값을 Integer로 취급하여 1 증가시킨다. | INCR mycount                |
| DECR | 특정 키의 값을 Integer로 취급하여 1 감소시킨다. | DECR mycount                |
| MSET | 여러 키에 대한 값을 한번에 저장한다.           | MSET mine milk yours coffee |
| MGET | 여러 키에 대한 값을 한번에 얻어 온다.          | MSET mine yours             |

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

| 명령어    | 기능                              | 에졔                  |
|--------|---------------------------------|---------------------|
| LPUSH  | 리스트의 왼쪽(head)에 새로운 값을 추가한다.     | LPUSH mylist apple  |
| RPUSH  | 리스트의 오른쪽(tail)에 새로운 값을 추가한다.    | RPUSH mylist banana |
| LLEN   | 리스트에 들어있는 아이템 개수를 반환한다.         | LLEN mylist         |
| LRANGE | 리스트의 특정 범위를 반환한다.               | LRANGE mysql 0 -1   |
| LPOP   | 리스트의 왼쪽(head)에서 값을 삭제하고 반환한다.   | LPOP mylist         |
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

| 명령어       | 기능                         | 예제                    |
|-----------|----------------------------|-----------------------|
| SADD      | Set에 데이터를 추가한다.            | SADD myset apple      |
| SREM      | Set에 데이터를 삭제한다.            | SRM myset apple       |
| SCARD     | Set에 저장된 아이템 개수를 반환한다.     | SCARD myset           |
| SMEMBERS  | Set에 저장된 아이템을 반환한다.        | SMEMBERS myset        |
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

| 명령어     | 기능                                     | 예제                         |
|---------|----------------------------------------|----------------------------|
| HSET    | 한개 또는 다수의 필드에 값을 저장한다.                 | HSET user name bear age 10 |
| HGET    | 특정 필드의 값을 반환한다.                        | HGET user1 name            |
| HMGET   | 한개 이상의 필드 값을 반환한다.                     | HMGET user1 name age       |
| HINCRBY | 특정 필드 값을 Integer로 취급하여 저장한 숫자를 증가 시킨다. | HINCRBY user1 viewcount 1  |
| HDEL    | 한개 이상의 필드를 삭제한다.                       | HDEL user1 name age        |

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

| 명령어      | 기능 제                            | 예제                             |
|----------|---------------------------------|--------------------------------|
| ZADD     | 한개 또는 다수의 값을 추가 또는 업데이트 한다.     | ZADD myrank 10 apple 20 banana |
| ZRANGE   | 특정 범위의 값을 반환한다.(오름차순으로 정렬된 기준)  | ZRANGE myrank 0 1              |
| ZRANK    | 특정 값의 위치(순위)를 반환한다.(오름차순으로 정렬된) | ZRANK myrank apple             |
| ZREVRANK | 특정 값의 위치(순위)를 반환한다.(내림차순으로 정렬된) | ZREVRANK myrank apple          |
| ZREM     | 한개 이상의 값을 삭제한다.                 | ZREM myrank apple              |

<details>
<summary>접기/펼치기</summary>

```
ZADD myrank 10 apple 20 banana 30 grape

ZRANGE myrank 0 1

ZREVRANK myrank apple

ZRANK myrank apple
```

</details>


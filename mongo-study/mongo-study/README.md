# MongoDB 개요

## SQL vs NoSQL

### RDBMS 장단점

* 장점
  * 데이터 중복을 방지할 수 있다.
  * join 성능이 좋다
  * 복잡하고 다양한 쿼리가 가능하다
  * 잘못 입력을 방지할 수있다.
* 단점
  * 하나의 레코드를 확인하기 위해 여러 테이블을 join 하여 가시성이 떨어진다.
  * 스키마가 엄격하여 변경에대한 공수가 크다.
  * Scale-Out이 가능하지만, 설정이 어려워 전통적으로 Sale-Up 위주로 확장 했다.
  * 확장할 때마다 App단의 수정이 필요하다

### NoSQL 장단점

* 장점
  * 데이터 접근성과 가시성이 좋다.
  * Join없이 조회가 가능해서 응답 속도가 일반적으로 빠르다.
  * 스키마 변경에 공수가 적다
  * 스키마가 유연해서 데이터 모델을 App 요구사항에 맞게 데이터를 수용할 수 있다.
  * HA와 Sharding에 대한 솔루션을 자체적으로 지원하고 있어 Scale-Out이 간편하다.
  * 확장시 App의 변셩사항이 없다.
* 단점
  * 데이터중복이 발생한다.
  * 스키마가 자유롭지만, 스키마 설계를 잘해야 성능 저하를 피할 수 있다.

### Summary

* MongoDB는 Document 지향 Database이다.
* 데이터 중복이 발생할 수 있지만, 접근성과 가시성이 좋다.
* 스키마 설계가 어렵지만, 스키마가 유연해서 App의 요구사항에 맞게 데이터를 수용할 수 있다.
* 분산에 대한 솔루션을 자체적으로 지원해서 Scale-Out이 쉽다.
* 확장 시, App를 변경하지 않아도 된다.

## MongoDB 구조

### RDBMS vs MongoDB


| RDBMS    | MongoDB    |
| -------- | ---------- |
| Cluster  | Cluster    |
| Database | Database   |
| Table    | Collection |
| Row      | Document   |
| Column   | Field      |

### 기본 Database

| Database | Description                                                                                                                                                                     |
| -------- |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| admin    | * 인증과 권한 부여 역할이다.<br/> * 일부 관리 작업을 하려면 admin database 접근이 필요하다.                                                                                                                 |
| local    | * 모든 mongodb instance는 local database를 소유한다.<br/> * oplog와 같은 replication 절차에 필요한 정보를 저장한다. <br/> * startup_log와 같은 instance 진단 정보를 저장한다. <br/> * local database는 자체는 복제되지 않는다. |
| config   | * shareded cluster에 각 shard 정보를 저장한다.                                                      ㅇ                                                                                    |

### Collection 특징

* 동적 스키마를 갖고 있어 스키마를 수정하려면 필드 값을 추가/수정/삭제 하면 된다.
* Collection 단위로 Index를 생성할 수 있다.
* Collection 단위로 Shard를 나눌 수 있다.

### Document 특징

* JSON 형태로 표한하고 BSON(Binary JSON) 형태로 저장한다.
* 모든 Document는 "_id" 필드가 있고, 없이 생성하면 ObjectId 타입의 고유한 값을 저장한다.
* 생성시, 상위 구조인 Database나 Collection이 없다면 먼저 생성하고 Document를 생성한다.
* Document의 최대 크기는 **16MB** 이다.


## MongoDB 배포 형태 소개

## Replica Set

## Replica Set vs Sharded Cluster 어떻게 배포할것인가?

## MogoDB Storage Engines


| 항목             | MMAPv1                                         | WiredTiger           |
| ---------------- | ---------------------------------------------- | -------------------- |
| Data Compression | 지원하지 않는다.                               | 지원한다.            |
| Locl             | 버전에 따라 Database 혹인 Collection 레벨 Lock | Document 레벨의 Locl |

# MongoDB Atlas 소개 및 실습 환경 셋팅

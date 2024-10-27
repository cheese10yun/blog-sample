아래는 `spring.data.mongodb` 설정 옵션과 그에 해당하는 기본값을 테이블 형태로 정리한 것입니다:

| 설정 옵션                                         | 설명                                       | 기본값                                       |
|-----------------------------------------------|------------------------------------------|-------------------------------------------|
| `spring.data.mongodb.uri`                     | MongoDB 연결 URI                           | `mongodb://localhost:27017/database_name` |
| `spring.data.mongodb.authentication-database` | 인증에 사용할 데이터베이스 이름                        | 없음                                        |
| `spring.data.mongodb.auto-index-creation`     | 인덱스를 자동으로 생성할지 여부                        | `false`                                   |
| `spring.data.mongodb.database`                | 연결할 데이터베이스 이름                            | 없음                                        |
| `spring.data.mongodb.field-naming-strategy`   | 필드 네이밍 전략 (예: CamelCase to snake_case)   | 없음                                        |
| `spring.data.mongodb.gridfs.bucket`           | GridFS 버킷 이름                             | `fs`                                      |
| `spring.data.mongodb.gridfs.database`         | GridFS 데이터베이스 이름                         | 없음                                        |
| `spring.data.mongodb.host`                    | Mongo 서버 호스트                             | `localhost`                               |
| `spring.data.mongodb.password`                | Mongo 서버 로그인 비밀번호                        | 없음                                        |
| `spring.data.mongodb.port`                    | Mongo 서버 포트                              | `27017`                                   |
| `spring.data.mongodb.replica-set-name`        | 사용할 복제 세트 이름                             | 없음                                        |
| `spring.data.mongodb.repositories.type`       | 사용할 Mongo 레포지토리 타입 (`auto`, `none`)      | `auto`                                    |
| `spring.data.mongodb.username`                | Mongo 서버 로그인 사용자명                        | 없음                                        |
| `spring.data.mongodb.uuid-representation`     | UUID 표현 방식 (`java-legacy`, `standard` 등) | `java-legacy`                             |

이 테이블은 `spring.data.mongodb` 관련 설정을 정의할 때 기본값을 참고하여 적절하게 설정할 수 있도록 도와줍니다. 필요한 경우 환경에 맞게 수정하여 사용할 수 있습니다.

```yml
spring:
    data:
        mongodb:
            uri: mongodb://localhost:27017/database_name # 기본값: 로컬호스트에 연결
            authentication-database: null                 # 기본값: 없음 (기본적으로 연결한 DB 사용)
            auto-index-creation: false                    # 인덱스 자동 생성 여부
            database: null                                # 연결할 데이터베이스 (기본값: 없음)
            field-naming-strategy: null                   # 필드 네이밍 전략 (기본값: 없음)
            gridfs:
                bucket: fs                                  # GridFS 버킷 이름 (기본값: fs)
                database: null                              # GridFS 데이터베이스 이름 (기본값: 없음)
            host: localhost                               # Mongo 서버 호스트 (기본값: localhost)
            password: null                                # Mongo 서버 로그인 비밀번호
            port: 27017                                   # Mongo 서버 포트 (기본값: 27017)
            replica-set-name: null                        # 복제 세트 이름 (기본값: 없음)
            repositories:
                type: auto                                  # Mongo 레포지토리 활성화 여부 (기본값: auto)
            username: null                                # Mongo 서버 로그인 사용자명 (기본값: 없음)
            uuid-representation: java-legacy              # UUID 표현 방식 (기본값: java-legacy)

```
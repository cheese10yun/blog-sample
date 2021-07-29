# Spring Config Server

![](images/config-server1.png)

스프링 Config Server는 각 애플리케이션에의 Config 설정을 중앙 서버에서 관리를 하는 서비스입니다. 중앙 저장소로 Github Repository뿐만 아니라 아래와 같은 저장소 환경을 제공해 주고 있습니다.

* Git(Github)
* JDBC
* REDIS
* AWS S3
* 등등..

본 포스팅은 Github Repository 저장소 기반으로 설명드리겠습니다. Github을 사용하고 계시다면 중앙 저장소로 Github Repository를 권장 드립니다.

Spring Config Server를 이용하면 `/actuator/refresh`, `/actuator/busrefresh`를 통해서 **서버를 재배포 없이 설정값을 변경할 수 있다는 점이 가장 큰 장점이라고 생각합니다.**

## Config Server 구성

```
dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation(org.springframework.boot:spring-boot-starter-actuator)
}
```

```kotlin
@SpringBootApplication
@EnableConfigServer // Config Server를 활성화 하기 위해 추가
class ConfigServerApplication

fun main(args: Array<String>) {
    runApplication<ConfigServerApplication>(*args)
}
```

```yml
server:
    port: 8888

spring:
    application:
        name: config-server
    
    cloud:
        config:
            server:
                encrypt.enabled: true
                git:
                    uri: Github Repositroy 주소 # ex https://github.com/cheese10yun/blog-sample
                    username: username
                    password: password
```

빠르게 확인하기 위해서 user name, password 기반으로 동작 시킵니다. 만약 해당 Repository가 Public이라면 생략 가능합니다. Private 경우에는 password 방식, SSH Key 인증 방식이 있습니다. SSH Key 인증 방식은 아래에서 살펴보겠습니다. 그리고 가능하면 Config Server Repository에 설정을 두는 것보다 Config 설정만 관리하는 Repository를 하나 생성하고 그쪽에는 순수하게 Config 설정들만 관리하는 것이 **코드와 설정 파일을 분리하는 좋은 방법이라고 생각합니다.**

```yml
# order-service.yml
message:
    profile: "default"
    server-name: "Config Server"

# order-service-sandbox.yml
message:
    profile: "sandbox"

# order-service-production.yml
message:
    profile: "production"

```

`{application-name}-{evn}.yml` 형식으로 각 profile에 본인의 환경을 설정했습니다. [msa-study-sample Repositroy](https://github.com/cheese10yun/msa-study-sample)에 저장되어 있습니다.


HTTP 서비스에는 다음과 같은 형식의 리소스가 표현됩니다.
```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

Github Repository를 이용한다면 `[/{label}]`은 branch 명으로 생각하면 됩니다. 한번 Config Server를 통해서 구동해 테스트해보겠습니다.


### /{application}/{profile}[/{label}] 조회
```
GET http://localhost:8888/order-service/local/master

HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 29 Jul 2021 17:37:11 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "name": "order-service",
  "profiles": [
    "local"
  ],
  "label": "master",
  "version": "93e0c93772d4c743eb888bcd9c14629eb1fa9551",
  "state": null,
  "propertySources": [
    {
      "name": "git@github.com:cheese10yun/msa-study-sample.git/order-service-local.yml",
      "source": {
        "message.profile": "local"
      }
    },
    {
      "name": "git@github.com:cheese10yun/msa-study-sample.git/order-service.yml",
      "source": {
        "message.profile": "default",
        "message.server-name": "Config Server"
      }
    }
  ]
}

Response code: 200; Time: 2957ms; Content length: 422 bytes

```

`/{application}/{profile}[/{label}]`는 `/order-service/local/master`을 의미합니다. label은 Github Repository 저장소를 이용하는 경우 branch로 생각하면 됩니다.

local 프로필을 조회했기 때문에 `message.profile=local`이 정상적으로 출력된 것을 확인할 수 있습니다. `propertySources` 객체가 배열로 default 프로필인 `order-service.yml` 값에 해당하는 `message.profile=default` 값도 같이 조회하는 것을 확인할 수 있습니다. **이는 특정 프로필에 없는 값은 기본 프로필의 값으로 대체되기 때문에 조회하는 것입니다. 만약 local 프로필에 없는 `message.server-name=Config Server` 값이 필요한 경우에는 default 값을 사용합니다.**

### /{application}-{profile}.yml 조회

```
GET http://localhost:8888/order-service-local.yml

HTTP/1.1 200 
Content-Type: text/plain
Content-Length: 55
Date: Thu, 29 Jul 2021 17:40:36 GMT
Keep-Alive: timeout=60
Connection: keep-alive

message:
  profile: local
  server-name: Config Server


Response code: 200; Time: 2335ms; Content length: 55 bytes

```

`/order-service-local.yml` 조회를 진행하게 되면 본인 profile의 값, default 값을 출력하는 것을 확인할 수 있습니다. `/{label}/{application}-{profile}.yml` 조회도 동일한 응답을 받습니다.

```
GET http://localhost:8888/order-service-local.properties

HTTP/1.1 200 
Content-Type: text/plain
Content-Length: 57
Date: Thu, 29 Jul 2021 17:45:34 GMT
Keep-Alive: timeout=60
Connection: keep-alive

message.profile: local
message.server-name: Config Server

Response code: 200; Time: 2430ms; Content length: 57 bytes

```

`/{application}-{profile}.properties`, `/{label}/{application}-{profile}.properties`도 위와 비슷한 응답을 받지만 위 처럼 `properteis`의 형식 입니다.


## Private Repository SSH Key

Public Repository는 누구나 접근 가능하니 상관없지만 Private Repository는 해강 Repository의 권한이 있는 사용자인지 확인해야 합니다. 대부분 실무 환경에서는 Private 환경이니 해당 Repository에 대한 검증이 필요합니다. password 방식은 password를 그대로 노출하니 SSH Key 방식을 선택하는 것이 바람직합니다.

### SSH Key 생성

```
ssh-keygen -m PEM -t rsa -b 4096 -f ~/config_server_deploy_key.
rsa
```

### Github SSH Key 등록

![](images/github-ssh.png)

![](images/github-ssh-1.png)

`SSH and GPG keys` -> `New SSH Key`으로 SSH Key의 공개키를 등록합니다

#### yml 설정

```yml
spring:
    cloud:
        config:
            server:
                git:
                    uri: git@github.com:cheese10yun/blog-sample.git
                    ignoreLocalSshSettings: true
                    private-key: |
                        -----BEGIN RSA PRIVATE KEY-----
                        ....
                        -----END RSA PRIVATE KEY-----
```
**uri 설정을 반드시 SSH 주소로 입력해야 합니다.** ignoreLocalSshSettings 설명은 아래 공식 레퍼런스를 확인해 주세요
> [Git SSH configuration using properties](https://cloud.spring.io/spring-cloud-config/reference/html/#_git_ssh_configuration_using_properties)
> 
>  For those cases, SSH configuration can be set by using Java properties. In order to activate property-based SSH configuration, the spring.cloud.config.server.git.ignoreLocalSshSettings property must be set to true, as shown in the following

![](images/github-ssh-2.png)

SSH 주소는 Github Code 버튼을 누르면 확인할 수 있습니다.

## 정리
Spring Config Server에 대해서 간단하게 알아봤습니다. 이후 포스팅에서는 각 애플리케이션(클라이언트)이 Config Server에 접속해서 설정값을 가져오는 방식, 배포 없이 Config 값을 변경하는 방법, 여러 서버가 구동 중인 경우 Kafka 기반으로 Broadcast 해서 Config 값 변경 전파하는 방식에 대해서 다뤄보겠습니다.
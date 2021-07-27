# Spring Config Server

![](images/config-server1.png)

스프링 Config Server는 각 애플리케이션에의 Config 설정을 중앙 서버에서 관리를 하는 서비스입니다. 중앙 저장소로 Github Repository 뿐만 아니라 아래와 같은 저장소 환경을 제공 해주고 있습니다.

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

빠르게 확인 하기 위해서 username, password 기반으로 동작 시킵니다. 만약 해당 Repository가 Public 이라면 생략 가능합니다. Private 경우에는 password 방식, SSH Key 인증 방식이 있습니다. SSH Key 인증 방식은 아래에서 살펴보겠습니다. 그리고 가능하면 Config Server Repository에 설정을 두는 것보다 Config 설정만 관리하는 Repository를 하나 생성하고 그 쪽에는 순수하게 Config 설정들만 관리하는 것이 **코드와 설정 파일을 분리하는 좋은 방법이라고 생각합니다.**

```yml
# order-service.yml
message:
    profile: "default"

# order-service-sandbox.yml
message:
    profile: "sandbox"

# order-service-production.yml
message:
    profile: "production"

```
`{application-name}-{evn}.yml` 형식으로 각 profile에 본인의 환경을 설정 했습니다.


HTTP 서비스에는 다음과 같은 형식의 리소스가 있습니다.
```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

Github Repository를 이용한다면 `[/{label}]`은 branch명으로 생각하면 됩니다. 한번 Config Server를 통해서 구동해 테스트 해보겠습니다.

## Spring Config Server 구성 하기
* [ ] 의존성 설명
* [ ] @EnableConfigServer
* [ ] yml 설정 관련등등
* [ ] Private Repo 환경 ID/Password, SSH
* [ ] bootstrap.yml 설정 config 서버정도 설정하는듯
* [ ] 애플리케이션 이름과 컨피스 서버의 config 이름과 동일 해야함
* [ ] searchPath의미가 있나??
* [ ] @RefreshScope, @ConfigurationProperties 도 자동으로 가능한듯?


### Private Repository SSH Key

Public Repository는 누구나 접근가능하니 상관 없지만 Private Repository는 해강 Repository의 권한이 있는 사용자인지 확인해야합니다. 대부분 실무 환경에서는 Private 환경이니 해당 Repository에 대한 검증이 필요합니다. password 방식은 password를 그대로 노출하니 SSH Key 방식을 선택하는 것이 바람직합니다.

#### SSH Key 생성

```
ssh-keygen -m PEM -t rsa -b 4096 -f ~/config_server_deploy_key.
rsa
```

#### Github SSH Key 등록

![](images/github-ssh.png)


![](images/github-ssh-1.png)

`SSH and GPG keys` -> `New SSH Key` 으로 SSH Key의 공개키를 등록 합니다

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
**uri 설정을 반드시 SSH 주소로 입력 해야합니다.** ignoreLocalSshSettings 설정은 아래 공식 레퍼런스 확인
> [Git SSH configuration using properties](https://cloud.spring.io/spring-cloud-config/reference/html/#_git_ssh_configuration_using_properties)
>  For those cases, SSH configuration can be set by using Java properties. In order to activate property-based SSH configuration, the spring.cloud.config.server.git.ignoreLocalSshSettings property must be set to true, as shown in the following

![](images/github-ssh-2.png)

SSH 주소는 Github Code 버튼을 누르면 확인할 수 있습니다.




# 테스트 시나리오

1. 동적으로 변경 확인
2. 새로 부팅시 config-server의 yml이 우선순위가 더 높음
3. 로컬 서버에 없는 경우는 config-server 기반으로 설정 가져옴
4. 로깅 레벨 변경?


# TODO
* [ ] Config 서버의 간단하 소개,
  * [ ] 중앙 집중
  * [ ] 동적 변경
  * [ ] 이미지 
* [ ] Private Repo SSH 관리
* [ ] 동적 변경

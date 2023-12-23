# Spring Boot3 Kotlin JPA & Querydsl 적용하기 

Spring Boot 2에서 3으로 업데이트되면서 Spring Data JPA 관련 설정이 변경된 부분들이 있습니다. 프로젝트에서 손쉽게 Spring Boot 3으로 업데이트하면서 Spring Data JPA와 Querydsl 설정을 손쉽게 하는 방법에 대해서 살펴보겠습니다. 

## 사전 설정

```Bash
$ ./gradlew wrapper --gradle-version=8.5
```
Gradle Wrapper를 사용하는 경우, 사용하고 있는 버전을 8.5 이상으로 업데이트 및 IntelliJ를 사용하는 경우 프로젝트의 SDK 버전을 17 이상으로 설정하는 과정을 진행합니다.

![](https://raw.githubusercontent.com/cheese10yun/IntelliJ/master/image/Project-Structure.png)

Project Structure 설정에서 SDK, Language Level을 17 버전 이상으로 지정합니다.
![](https://raw.githubusercontent.com/cheese10yun/IntelliJ/master/image/module.png)

Module SDK 버전도 동일한 버전으로 설정합니다.

![](https://raw.githubusercontent.com/cheese10yun/IntelliJ/master/image/gradle.png)

Gradle 마지막으로 gradle 버전도 동일한 버전으로 설정합니다.

## build.gradle.kts

```Gradle
plugins {
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    kotlin("plugin.jpa") version "1.9.21"
}
```
* 스프링 부트 버전 `3.2.1`으로 설정합니다.
* 코틀린 버전 `1.9.21`으로 설정합니다.

```Gradle
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

build.gradle.kts 설정에 각종 java version을 사전 설정과 동일한 버전으로 설정합니다.


## Import Replace

Spring Data JPA에서의 주요 변경사항 중 하나는 패키지 경로의 변경입니다. 이전에 사용되던 `javax.persistence`가 `jakarta.persistence`로 업데이트되었습니다. IntelliJ의 Replace 기능을 이용하면 프로젝트 내의 모든 import 경로를 쉽게 변경할 수 있습니다. `cmd + shift + r` 단축키로 Replace 설정을 할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/import-repace.png)

`import javax.persistence` -> `jakarta.persistence` 작성한 이후 `REPLACE` 버튼으로 적용 합니다.

## Querydsl 적용

```Gradle
dependencies {
    // implementation("com.querydsl:querydsl-jpa")
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    
    // kapt("com.querydsl:querydsl-apt:5.0.0:jpa")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
}
```

기존 querydsl 의존성도 변경을 변경을 진행합니다. 

```Bash
$ ./gradlew build -x test  
```

해당 프로젝트를 빌드하면 QClass가 생성되는 것을 확인할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/qclass.png)
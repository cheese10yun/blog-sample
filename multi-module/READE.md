# Gradle 기반 멀티 모듈

## 멀티 모듈 구성
```
├── admin-api
│   ├── out
│   └── src
├── api
│   ├── out
│   └── src
├── build.gradle
├── core
│   ├── build
│   ├── out
│   └── src
├── gradle
│   └── wrapper
├── gradlew
├── gradlew.bat
└── settings.gradle
```

* admin-api : admin 관련 API
* api : 일반 유저 api
* core : domain 및 공통 모듈


## build.gradle : 스프링 디펜던시 정의
```gradle
buildscript {
    ext {
        springBootVersion = '2.1.5.RELEASE'
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        classpath "io.spring.gradle:dependency-management-plugin:1.0.6.RELEASE"
    }
}
```
기본적인 스프링 디펜던시를 정의 합니다.

## settings.gradle

```
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
rootProject.name = 'multi'

include 'core'
include 'api'
include 'admin-api'
```
추가할 서브모듈을 include 합니다.

## build.gradle : subprojects, project 정의

```
subprojects {
    apply plugin: 'java'
    apply plugin: 'eclipse'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    group = 'com.module'
    version = '0.0.1-SNAPSHOT'
    sourceCompatibility = '1.8'

    repositories {
        mavenCentral()
    }

    // 서브 모듈에 공통으로 의존성을 추가 시킬 디펜던시를 정의합니다.
    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        implementation 'org.springframework.boot:spring-boot-starter-actuator'
        implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    }

   // 해당 task로 서브 모듈 추가시 기초적인 디렉토리를 자동으로 생성해줍니다.
    task initSourceFolders {
        sourceSets*.java.srcDirs*.each {
            if (!it.exists()) {
                it.mkdirs()
            }
        }

        sourceSets*.resources.srcDirs*.each {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }

}

// project를 정의합니다. 공통으로 사용되는 core 모듈을 추가해 줍니다.
project(':core') {
    dependencies {
        compile('com.h2database:h2')
    }
}

project(':api') {
    dependencies {
        compile project(':core')
        implementation 'org.springframework.boot:spring-boot-starter-web'
    }
}

project(':admin-api') {
    dependencies {
        compile project(':core')
        implementation 'org.springframework.boot:spring-boot-starter-web'
    }
}
```
해당 gradle 파일을 작성하면 [Gradle 기반 멀티 모듈](#gradle-%EA%B8%B0%EB%B0%98-%EB%A9%80%ED%8B%B0-%EB%AA%A8%EB%93%88) 처럼 자동으로 디렉토리가 생성됩니다.

## Core Module


```java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name", nullable = false)
  private String name;

  public Member(String name) {
    this.name = name;
  }
}

public interface MemberRepository extends JpaRepository<Member, Long> {}
```

간단한 Entity, Repository를 만듭니다.

## Api Module

```java
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberApi {
  
  private final MemberRepository memberRepository;

  @PostMapping
  public Member create() {
    final Member member = new Member("test");
    return memberRepository.save(member);
  }

  @GetMapping
  public List<Member> getMembers() {
    return memberRepository.findAll();
  }
}
```

![](https://github.com/cheese10yun/blog-sample/blob/master/multi-module/imags/create-member.png?raw=true)
![](https://github.com/cheese10yun/blog-sample/blob/master/multi-module/imags/get-members.png?raw=true)

Member 생성, 조회가 제대로 동작하는 것을 보아 Core 모듈에 있는 MemberRepository가 제대로 동작하는 것을 확인할 수 있습니다.


## Admin Module

```java
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberApi {


  private final MemberRepository memberRepository;

  @GetMapping
  public List<Member> getMembers() {
    return memberRepository.findAll();
  }

  @PostMapping
  public Member create() {
    final Member member = new Member("test");
    return memberRepository.save(member);
  }
}
```
![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/multi-module/imags/admin-member.png)

Admin 모듈도 동일하게 API 구성하고 테스트 하여도 동일합니다.

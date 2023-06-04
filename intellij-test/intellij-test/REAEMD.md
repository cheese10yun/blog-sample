# Intelli J

## Presentation

<p align="center">
<img src="images/tip-6.png" width="800">
</p>


[Presentation Assistant](https://plugins.jetbrains.com/plugin/7345-presentation-assistant) 플러그인을 통해서 단축키 정보는 하단에 출력 됩니다.


## Tab

### Tab Limit

<p align="center">
<img src="https://raw.githubusercontent.com/cheese10yun/blog-sample/master/intellij-test/intellij-test/images/tip-1.png" width="800">
</p>


* settings -> editor tabs -> Tab Limit 으로 Tab limit 설정 가능
* 여러 Tab을 켜도 Limit한 설정 값으로 유지, Limit 1을 추천

### Tab 이동


<p align="center">
<img src="https://raw.githubusercontent.com/cheese10yun/blog-sample/master/intellij-test/intellij-test/images/tip-2.png" width="800">
</p>

| Name                     | Hot Key         | Desc                                         |
|--------------------------|-----------------| -------------------------------------------- |
| `Split Right`            | fn + ctr +  ➡️  | 현재 화면 오른 쪽으로 분활                   |
| `Split Down`             | fn + ctr +  ⬇️  | 현재 화면 아래 쪽으로 분할                   |
| `Goto Next Splitter`     | shfit + cmd  ➡️ | 현재 포커싱 화면애서 다음 Tab으로 이동       |
| `Goto Previous Splitter` | shfit + cmd  ⬅️ | 현재 포커싱 화면애서 이전 다음 Tab으로 이동  |
| `<- Back`                | cmd + [         | 현재 Tab의 이전 Tab, tab limit 1 지정시 유용 |
| `-> Forword`             | cmd + ]         | 현재 Tab의 다음 Tab, tab limit 1 지정시 유용 |
| `Recent Files`           | cmd + e         | 최근 Open 파일 리스트                        |
| `Recent Locations`       | shfit + cmd + e | 최근 Open 파일 커서 위치                     |
| `Bookmarks 지정`           | cmd + F3        | 북마크 지정                                  |
| `Bookmarks`              | F3              | 지정한 북마크 리스트                         |
| `Go to file`             | shfit + cmd + o | 파일 열기                                    |


## 간단 팁

### Find Action

<p align="center">
<img src="https://raw.githubusercontent.com/cheese10yun/blog-sample/master/intellij-test/intellij-test/images/tip-3.png" width="800">
</p>

* 특정한 기능을 찾고 싶은 경우 단축키 `shfit + cmd + a`  Find Action 으로 해당 기능을 찾을 수 있음
* 대충 이런 기능이 있지 않을까 하는 기능을 검색을 통해서 해당 기능의 유무를 빠르게 파악 가능 ex) git stash


### Key map

<p align="center">
<img src="https://raw.githubusercontent.com/cheese10yun/blog-sample/master/intellij-test/intellij-test/images/tip-4.png" width="800">
</p>

* Key map 통해서 Hot Key 조회 및 등록 가능
* 키워드를 통해 검색 or 단축 키를 통한 검색도 지원


### Live Template

![](https://camo.githubusercontent.com/5471e679d04d13a643230014376ab5e736a72b4252a10e644eb41e158b3e5ea4/68747470733a2f2f692e696d6775722e636f6d2f6d5a75444d64552e676966)

* 코드 템플릿을 미리 지정해서 편하게 코드를 작성할 수 있는 기능입니다.
* `sout`, `psvm` 등이 여기에 해당 합니다.
* `ss`, `tdd`, `jobc`, `jobcode`, `comment-formatter` `sf` 등등을 커스텀해서 사용

#### 등록 방법

![](https://camo.githubusercontent.com/05c612752077fb8488efe81f76609936965dfeb0340540bdc7b45d8040cd5524/68747470733a2f2f692e696d6775722e636f6d2f483471523461612e706e67)

* Settings -> Live Templates

### formatter 코드 정렬 제외

![](images/tip-8.png)

```kotlin
fun asd(): Unit {
    //@formatter:off
    val a = 10
        val b = 10
        val c = 10
        val d = 10
    val e = 10
    val f = 10
    //@formatter:on
}
``` 
* 특정한 이유로 코드 정렬을 제외하고 싶을 때 `@formatter:off ~ @formatter:on`을 활용하여 제외할 수 있다.

### Gradle Task

![](https://raw.githubusercontent.com/cheese10yun/IntelliJ/master/assets/gradle-task-run-1.gif)

* Gradle Task 자동 완성 기능을 통해서 보다 쉽게 Gradle 명령어를 사용할 수 있습니다.
* `build.gradle.kts`에 직접 작성한 TASK도 동작 가능


## 문자열

### 동일 문자열 ⌘ + ⌃ + g

![](https://camo.githubusercontent.com/6980a81c4101651570d8f5d1d5f0ccfe951ec6c37af4b7856b4486ec5d92bdd3/68747470733a2f2f692e696d6775722e636f6d2f66414d41364f722e676966)


### 동일 위치열 ⌥ + drag or ⌥⌥  

![](https://camo.githubusercontent.com/208370c96ef96e70a051b984025f8d954679d08cb32abbe724f6a39f5cb0ffb1/68747470733a2f2f692e696d6775722e636f6d2f5a3549596736772e676966)

### 복사 히스토리 command + shift + v

<p align="center">
<img src="https://raw.githubusercontent.com/cheese10yun/blog-sample/master/intellij-test/intellij-test/images/tip-7.png" width="800">
</p>

### 활용 방법

![](images/tip-14.gif)

특정 Entity를 응답 객체로 만들어야하는 경우 동일 문자열을 복사해서 보다 쉽게 Response 객체를 만들수 있습니다.


## Plugins
* [ ] grep
* [ ] git tool box
* [ ] string manipulation
* [ ] Rainbow
* [ ] Key Promoter X
* [ ] GitToolbox


### String Manipulation Plugin

[](https://plugins.jetbrains.com/plugin/2162-string-manipulation/)

![](https://raw.githubusercontent.com/cheese10yun/IntelliJ/master/assets/string-manipulation-1.gif)

* 특정 값에 대해서 자동으로 증가시켜 중복되지 않는 값으로 설정할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/kotlin-jpa/docs/string-manipulation-3.gif)

* Switch는 다양한 문자열 포맷으로 쉽게 변경이 가능합니다.

## HTTP Client

![](images/tip-9.gif)

HTTP Client는 Postman 처럼 HTTP 호출을 도와주는 도구로 IntelliJ에서 사용 가능

### HTTP Env

![](images/tip-10.png)

```json
{
  "local": {
    "shop-service": "http://localhost:8080"
  },
  "sandbox": {
    "shop-service": "http://snadbox-xxxx"
  },
  "beta": {
    "shop-service": "http://beta-xxxx"
  }
}
```

* HTTP 호출 환경을 JSON으로 관리할 수 있습니다.

### HTTP Curl Copy & Paste

![](images/tip-11.gif)

* Curl을 붙여넣를 진행하면 HTTP Client에 맞는 형식으로 붙여 자동으로 변환 됩니다.

![](images/tip-12.gif)

* HTTP Client를 Curl 형식으로 변환하여 복사할 수 있습니다.

### Query parameter 정리

![](images/tip-13.gif)

* Query parameter를 쉽게 정리할 수 있습니다.


## Spring

### Debug

* [ ] Beans
* [ ] Health
* [ ] Mappings

### Spring
* [ ] Beans
* [ ] MVC
* [ ] Data

### Endpoints
* [ ] Bean
* [ ] MVC


### Dependencies
* [ ] Version

## Github Code Review

* [ ] CMD + 3
* [ ] comment
* [ ] Approve

## Git
* [ ] Commit, Push, Pull
* [ ] Stash, UnStash
* [ ] Branch, Branch Diff, Merge, Checkout

## Refactoring

* [ ] move
* [ ] copy
* [ ] introduce variable
* [ ] Property
* [ ] introduce Parameter
* [ ] Pull Members Up

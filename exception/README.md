# Checked Exception을 대하는 자세

자바에서는 대표적으로 Checked *Exception* 과 Unchecked Exception이 있습니다. 먼저 이 둘의 차이를 살펴보겠습니다.


## Checked *Exception*, Unchecked *Exception* 차이

![](https://github.com/cheese10yun/TIL/blob/master/draw/excpetion-class-diagram.png?raw=true)


Error는 시스템이 비정상적인 상황에서 발생하다. 이 수준의 Error는 시스템 레벨에서 발생하는 심각한 수준의 오류이기 때문에 개발자가 미리 예측할 수 도 없고 처리할 수 있는 방법도 없다. 애플리케이션단에서는 Error에 대한 처리를 신경쓰지 않아도 된다. OutOfMemoryError이나 ThreadDeath 같은 에러는 try catch으로 잡아도 할 수 있는 것이 없기 때문이다.





.                    | Checked *Exception*       | Unchecked *Exception*
---------------------|---------------------------|-----------------------------------------------
**처리 여부**            | 반드시 예외 처리 해야함             | 예외 처리 하지 않아도됨
**트랜잭션 Rollback 여부** | Rollback 안됨               | Rollback 진행
**대표 Exception**     | IOException, SQLException | NullPointerException, IllegalArgumentException

Checked, Unchecked은 개발자들이 만든 애플리케이션 코드에서 예외가 발생했을 경우에 사용 된다.

위 상속 구조를 처럼 **Unchecked Exception는 RuntimeException을 상속하고 Checked Exception는 RuntimeException을 상속하지 않는다.** 이것으로 두 Exception을 구분 할 수있다.



### Unchecked *Exception* 
명시적인 예외 처리를 강제하지 않는 특징이 있기 때문에 Unchecked Exception 이라고 한다. catch로 잡거나 throw로 호출한 메서드로 예외를 던지지 않아도 상관 없다.


### Checked *Exception*
반드시 명시적으로 처리해야 하기 떄문에 Checked Exception 이라고 한다. try catch를 해서 에러를 잡든 throws를 통해서 호출한 메서드로 예외를 던저야 한다.


### Code : 예외 처리 여부 

```java
  @Test
  public void throws_던지기() throws JsonProcessingException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final Member member = new Member("yun");
    final String valueAsString = objectMapper.writeValueAsString(member);

  }

  @Test
  public void try_catch_감싸기() {
    final ObjectMapper objectMapper = new ObjectMapper();
    final Member member = new Member("yun");
    final String valueAsString;
    try {
      valueAsString = objectMapper.writeValueAsString(member);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
```
위 JsonProcessingException는 IOException Exception을 상속하는 Checked Exception이다. **그러기 때문에 throws로 상위 메서드로 넘기든 자신이 try catch해서 throw를 던지든 해야한다.** 이것은 문법적인 강제 선택이다. 그에 반해 **Unchecked Exception은 명시적인 예외 처리를 하지 않아도 된다.**


### Code : Rollback 여부

```java
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;

  // (1) RuntimeException 예외 발생
  public Member createUncheckedException() {
    final Member member = memberRepository.save(new Member("yun"));
    if (true) {
      throw new RuntimeException();
    }
    return member;
  }

  // (2) IOException 예외 발생
  public Member createCheckedException() throws IOException {
    final Member member = memberRepository.save(new Member("wan"));
    if (true) {
      throw new IOException();
    }
    return member;
  }
}
```

(1) RuntimeException 예외 발생 발생 시키면 yun이라는 member는 rollback이 진행됩니다. 하지만 (2) IOException 예외 발생이 되더라도 wan은 **rollback이 되지 않고 트랜잭션이 commit까지 완료됩니다.**


```
--- Ynu Log
Hibernate: 
    /* insert yun.blog.exception.member.Member
        */ insert 
        into
            member
            (id, name) 
        values
            (null, ?)
2019-05-16 00:55:16.117 TRACE 49422 --- [nio-8080-exec-2] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [VARCHAR] - [yun]
2019-05-16 00:55:16.120 ERROR 49422 --- [nio-8080-exec-2] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.RuntimeException] with root cause

java.lang.RuntimeException: null

--- Wan Log
Hibernate: 
    /* insert yun.blog.exception.member.Member
        */ insert 
        into
            member
            (id, name) 
        values
            (null, ?)
2019-05-16 00:55:43.931 TRACE 49422 --- [nio-8080-exec-4] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [VARCHAR] - [wan]
2019-05-16 00:55:43.935 ERROR 49422 --- [nio-8080-exec-4] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception

java.io.IOException: null
	at yun.blog.exception.member.MemberService.createCheckedException(MemberService.java:27) ~[classes/:na]
```
로그 메시지를 보면 member yun, wan 모두 insert 쿼리는 보이지만 yun rollback이 진행되고 wan은 rollback이 되지 않고 commit까지 됩니다.


### 왜 Checked Exception은 Rollback되지 않는 것일까?
기본적으로 Checked Exception는 복구가 가능 하다는 메커니즘을 가지고 있다. 예를들어서 특정 이미지 파일을 찾아서 전송해주는 함수에서 이미지를 찾지 못했을 경우 기본 이미지를 전송한다. 복구 전략을 가질 수 있게 된다

```java
public void sendFile(String fileName){

    File file;
    try {
        file = FileFindService.find(fileName);
    } catch (FileNotFoundException e){ // FileNotFoundException은 IOException으로 checked exception이다.
        // 파일을 못찾았으니 기본 파일을 찾아서 전송 한다
        file = FileFindService.find("default.png");
    }

    send(file);
}
```
기본적으로 복구가 가능하니 니가 복구를 작업을 진행했을 수 있으니까 Rollback은 진행하지 않을게 라는 의미가 있다고 생각합니다. (주관적인 생각입니다.)

하지만 이런식의 예외는 복구하는 것이 아니라 일반적인 코드의 흐름으로 제어 해야합니다.
```java
public void sendFile(String fileName){
    if(FileFindService.existed(filename)){
        // 파일이 있는 경우 해당 파일을 찾아서 전송
        send(FileFindService.find(fileName));    
    }else{
        // 파일이 있는 없는 경우 기본 이미지 전송
        send(FileFindService.find("default.png"));    
    }
}
```

### 하지만 현실은...
하지만 우리가 일반적으로 Checked Exception 예외가 발생했을 경우 복구 전략을 갖고 그것을 복구할수 있는 경우는 그렇게 많지 않습니다.

유니크해야하는 이메일 값이 중복되서 SQLException이 발생하는 경우 어떻게 복구 전략을 가질 수 있을까요? 유저가 압력했던 이메일 + 난수를 입력해서 insert 시키면 가능은 하겠지만 현실에서는 그냥 RuntimeException을 발생 시키고 입력을 다시 유도하는 것이 현실적입니다.

**여기서 중요한 것은 해당 Exception을 발생시킬 때 명확하게 어떤 예외가 발생해서 Exception이 발생했는지 정보를 전달해주는 것입니다. 위 같은 경우에는 DuplicateEmailException (Unchecked Exception)을 발생시는 것이 바람직합니다.**

Checked Exception을 만나면 더 구체적인 Unchecked Exception을 발생시켜 정확한 정보를 전달하고 로직의 흐름을 끊어야 합니다. 우리는 JPA에 구현체를 가져다 사용하더라도 Checked Exception을 직접 처리하지 않고 있는 이유도 다 적절한 RuntimeException으로 예외를 던져주고 있기 때문입니다.

[Spring Exception Guide](https://github.com/cheese10yun/spring-guide/blob/master/docs/exception-guide.md)에서 정리한 내용의 일부입니다. 


### Try Catch 전략
기본적으로 예외가 발생하면 로직의 흐름을 끊고 종료 시켜야 합니다물론 예외도 있지만, 최대한 예외를 발생시켜 종료하는 것을 지향해야 한다고 생각합니다.)

Checked Exception 같은 경우에는 예외를 반드시 감싸야 하므로 이러한 경우에는 `try catch`를 사용해야 합니다.

```java
try {
    // 비지니스 로직 수행...
}catch (Exception e){
    e.printStackTrace();
    throw new XXX비지니스로직예외(e);
}
```
`try catch`를 사용해야 하는 경우라면 더 구체적인 예외로 Exception을 발생시키는 것이 좋습니다. 간단하게 정리하면

1. `try catch`를 최대한 지양해라
2. `try catch`로 에러를 먹고 주는 코드는 지양해라(이런 코드가 있다면 로그라도 추가해주세요...)
3. `try catch`를 사용하게 된다면 더 구체적인 Exception을 발생시키는 것이 좋다.




## 참고
* [Java 예외(*Exception*) 처리에 대한 작은 생각](http://www.nextree.co.kr/p3239/)
* [토비의 스프링](http://www.yes24.com/Product/goods/7516721)




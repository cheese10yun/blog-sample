#### Lazy 로딩을 위한 프록시 구성 방법

Kotlin에서는 클래스가 기본적으로 final로 선언되기 때문에, 특별한 설정 없이 작성된 클래스는 상속이 불가능합니다. Proxy 기반의 Lazy 로딩은 실제 객체 대신 프록시 객체를 생성하여 해당 객체의 속성에 접근할 때 실제 데이터를 로딩하는 방식으로 동작합니다. 이를 위해서는 대상 클래스가 open이어야 하는데, 만약 클래스가 final이면 프록시 객체를 생성할 수 없으므로 Lazy 로딩이 제대로 동작하지 않습니다.

예를 들어, Author 클래스가 final 상태라면 Spring Data MongoDB는 CGLIB를 사용해 해당 클래스를 상속하는 프록시 객체를 생성하려 할 때 다음과 같은 오류가 발생합니다.

```
java.lang.IllegalArgumentException: Cannot subclass final class com.example.mongostudy.dbref.Author
    at org.springframework.cglib.proxy.Enhancer.generateClass(Enhancer.java:660)
    ...
```

이를 해결하기 위해 Kotlin에서는 `all-open` 플러그인 또는 `kotlin-spring` 플러그인을 적용하여, 특정 애노테이션(예: `@Document`)이 붙은 클래스를 자동으로 open으로 변환할 수 있습니다. 아래 예시는 이러한 설정을 적용하는 방법을 보여줍니다.

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    // 또는 id("org.jetbrains.kotlin.plugin.allopen") ...
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}
```

이 설정을 적용하면, `@Document` 애노테이션이 붙은 클래스들은 자동으로 open으로 처리되어 프록시 객체 생성이 가능해집니다. 즉, Lazy 로딩이 원활하게 동작할 수 있습니다.

아래 이미지는 Lazy 로딩이 활성화된 상태에서 Author 필드를 지연 로딩하기 위해 생성된 LazyLoadingProxy 객체를 보여줍니다. 이 프록시 객체는 실제 Author 데이터에 접근할 때 필요한 시점에 데이터를 로딩하도록 구성되어 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-6.png)

결국, 도메인 클래스가 open 상태로 변환되지 않으면 프록시 객체 생성이 불가능하여 Lazy 로딩이 실패하게 됩니다. 따라서, Spring Data MongoDB에서 Lazy 로딩 기능을 활용하려면 반드시 위와 같은 설정을 통해 대상 도메인 클래스가 open 상태로 유지되도록 해야 합니다.
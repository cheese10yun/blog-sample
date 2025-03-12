#### Kotlin에서 Lazy 로딩을 위한 프록시 구성 방법

Kotlin에서는 클래스가 기본적으로 final로 선언됩니다. 즉, 특별한 설정 없이 작성된 클래스는 확장(상속)이 불가능한 상태입니다. Proxy 기반의 Lazy 로딩 기능은 실제 객체 대신 프록시 객체를 생성하여 해당 객체의 속성에 접근할 때 실제 데이터를 로딩하는 방식으로 동작하는데, 이를 위해서는 대상 클래스가 open이어야 합니다. 만약 클래스가 final이라면, 프록시 객체를 생성할 수 없으므로 Lazy 로딩이 제대로 동작하지 않습니다.

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

이 설정을 적용하면, `@Document` 애노테이션이 붙은 클래스들은 자동으로 open으로 처리되어 프록시 객체 생성이 가능해집니다. 즉, Lazy 로딩이 원활하게 동작할 수 있게 됩니다.

- **allOpen 미적용 시**: 클래스는 기본적으로 final 상태이며, 이로 인해 Proxy 기반의 Lazy 로딩이 불가능합니다.
- **allOpen 적용 시**: 클래스가 open으로 변환되어 Lazy 로딩을 위한 Proxy 객체 생성이 가능해집니다.

따라서, Spring Data MongoDB에서 Lazy 로딩 기능을 활용하려면 반드시 이러한 설정을 통해 대상 도메인 클래스가 open 상태로 유지되도록 해야 합니다.
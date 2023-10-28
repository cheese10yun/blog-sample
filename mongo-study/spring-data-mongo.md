
## MappingMongoConverter
`MappingMongoConverter`는 Spring Data MongoDB에서 도메인 객체와 MongoDB BSON 문서 간의 변환을 담당하는 핵심 클래스입니다. 여기에는 많은 사용자 정의 설정과 기능이 포함되어 있습니다. 주요한 설정들을 아래에 설명하겠습니다:

1. **Type Mapper (`typeMapper`)**:
    - Java 객체를 MongoDB BSON 문서로 저장할 때 해당 객체의 클래스 정보를 저장하는데 사용됩니다. 이렇게 저장된 클래스 정보는 후에 객체를 조회할 때 해당 객체의 클래스로 변환하는 데 필요합니다.
    - 기본적으로 `_class` 필드에 클래스 정보가 저장됩니다.
    - `DefaultMongoTypeMapper(null)`을 사용하면 `_class` 필드 저장을 비활성화할 수 있습니다.

2. **Custom Conversions (`customConversions`)**:
    - 사용자 정의의 변환 로직을 추가할 수 있습니다. 예를 들어, 특정 클래스를 다른 형식으로 저장하거나 반환하고 싶을 때 사용합니다.
    - `CustomConversions` 객체를 사용하여 변환 로직을 등록하고, 이를 `MappingMongoConverter`에 설정합니다.

3. **DbRef Resolving (`dbRefResolver`)**:
    - MongoDB의 DBRef는 한 컬렉션의 문서를 다른 컬렉션의 문서로 참조할 수 있게 해줍니다.
    - `MappingMongoConverter`는 `DbRefResolver`를 사용하여 이러한 참조를 해결합니다.

4. **Mapping Context (`mongoMappingContext`)**:
    - MongoDB 문서와 도메인 객체 간의 매핑 정보를 제공합니다.
    - 예를 들어, 어떤 도메인 필드가 어떤 MongoDB 문서 필드에 매핑되는지, 해당 필드의 타입은 무엇인지 등의 정보를 포함합니다.

5. **Null Value Checking Strategy (`nullValueCheckStrategy`)**:
    - 객체를 MongoDB 문서로 변환할 때 null 값을 어떻게 처리할 것인지 결정합니다.
    - 예를 들어, `nullValueCheckStrategy`를 `ALWAYS`로 설정하면, 객체의 모든 필드를 검사하고 null 값이면 해당 필드를 MongoDB 문서에 포함시키지 않습니다.

이 외에도 `MappingMongoConverter`는 많은 기능과 설정 옵션을 제공합니다. 해당 설정들은 사용 사례나 요구 사항에 따라 커스터마이징하고 최적화할 수 있습니다.
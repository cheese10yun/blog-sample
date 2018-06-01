# Jackson 어노테이션 사용법

* [Jackson Annotation Examples](http://www.baeldung.com/jackson-annotations) 예제를 적용전, 적용후로 나누어서 정리 해봤습니다.
* 테스트코드도 참고하시면 좋습니다.

## 목차
* Jackson Serialization Annotations
* Jackson Deserialization Annotations
* Jackson Property Inclusion Annotations (추가예정)
* Jackson Polymorphic Type Handling Annotations (추가예정)
* Jackson General Annotations (추가예정)
* Custom Jackson Annotation (추가예정)
* Jackson MixIn Annotations (추가예정)
* Disable Jackson Annotation (추가예정)




## 2. Jackson Serialization Annotations

## 2.1. @JsonAnyGetter 직렬화 할 때 Map의 모든 키 - 값 을 표준 일반 속성으로 가져옵니다
```java
@Getter
@Builder
public static class ExtendableBean {
    public String name;
    private Map<String, String> properties;

    @JsonAnyGetter
    public Map<String, String> getProperties() {
        return properties;
    }
}
```

```json
//적용전
{
  "name": "yun",
  "properties": {
    "key1": "value1",
    "key2": "value2"
  }
}
//적용후
{
  "name": "yun",
  "key1": "value1",
  "key2": "value2"
}
```

## 2.2. @JsonGetter getter 이름 기반으로 키값이 정해지는것을 어노테이션을 제어
```java
@Builder
public static class MyBean {
    public int id;
    private String name;

    @JsonGetter("name")
    public String getTheName() {
        return name;
    }
}
```
```json
//적용전
{
  "id": 1,
  "theName": "yun"
}
//적용후
{
  "id": 1,
  "name": "yun"
}
```

## 2.3. @JsonPropertyOrder 직렬화 순서를 제어
```java
@JsonPropertyOrder({"name", "id"})
@Builder
public static class PropertyOrder {
    private long id;
    private String name;
}
```

```json
//적용전
{
  "id": 1,
  "name": "name"
}
//적용후
{
  "name": "name",
  "id": 1
}
```

## 2.4. @JsonRawValue Jackson이 속성을 그대로 직렬화하여 JSON으로 변경
```java
@Builder
    public static class RawBean {
        public String name;

        @JsonRawValue
        public String json;
    }
```
```json
//적용전
{
  "name": "yun",
  "json": "{\n  \"attr\":false\n}"
}
//적용후
{
  "name": "yun",
  "json": {
    "attr": false
  }
}
```

## 2.5. @JsonValue getName 에 @JsonValue 해당 멤버필드가 이름을 통해 직렬화 시킴
```java
public enum TypeEnumWithValue {
    TYPE1(1, "Type A"),
    TYPE2(2, "Type 2");

    private Integer id;
    private String name;

    TypeEnumWithValue(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
```
```json
//적용전
"TYPE1"
//적용후
"Type A"
```

## 2.6. @JsonRootName Root 이름 지정
```java
@Builder
@JsonRootName(value = "user")
public static class UserWithRoot {
    public int id;
    public String name;
}

//objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE); 반드시 적용해야함
```
```json
//적용전
{
  "id": 1,
  "name": "yun"
}
//적용후
{
  "user": {
    "id": 1,
    "name": "yun"
  }
}
```

## 3. Jackson Deserialization Annotations

### @JsonCreator JSON key 와 멤버 필드의 이름이 일치하지 않을 경우 사용합니다.
```json
{
  "id":1,
  "theName":"My bean"
}
```

```java
public static class BeanWithCreator {
    public int id;
    public String name;

    @JsonCreator
    public BeanWithCreator(
            @JsonProperty("id") int id,
            @JsonProperty("theName") String name
    ) {
        this.id = id;
        this.name = name;
    }
}
```

### @JacksonInject JSON 데이터가 아닌 값을 주입하는데 사용됩니다.
```json
{
  "name": "My bean"
}
```

```java
public static class BeanWithInject {
    @JacksonInject
    public int id;

    public String name;
}
```

###  @JsonAnySetter Map을 이용해서 유연성있게 Deserialization 합니다.
```json
{
  "name": "My bean",
  "attr2": "val2",
  "attr1": "val1"
}
```

```java
public static class ExtendableBean {
    public String name;
    private Map<String, String> properties = new HashMap<>();


    @JsonAnySetter
    public void setProperties(String key, String value) {
        properties.put(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Map<String, String> getProperties() {
        return properties;
    }
}
```

### @JsonSetter 객체와 맴버필드와 일치하지 않을 경우 유용하게 사용할 수 있습니다.

```json
{
  "id": 1,
  "name": "My bean"
}
```
```java
public static class MyBean {
    public int id;
    private String name;

    @JsonSetter("name")
    public void setTheName(String name) {
        this.name = name;
    }

    public String getTheName() {
        return this.name;
    }
}
```

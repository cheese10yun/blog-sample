# Kotlin Json Minify

불필요한 공백, 개행 및 탭 문자를 제거하여 JSON 크기를 줄인다.

## AS IS
```json
{
  "name": "xxx",
  "address": {
    "address": "address",
    "address_detail": "address_detail_1",
    "zip_code": "111"
  }
}
```

## TO BE

```kotlin
{"name":"xxx","address":{"address":"address","address_detail":"address_detail_1","zip_code":"111"}}
```

## Code

```kotlin
/**
     * JSON을 minify 진행한다. 불필요한 공백, 개행 및 탭 문자를 제거하여 JSON 크기를 줄인다.
     *
     * @param json minify 이전 JSON
     * ```
     * {
     *   "name": "xxx",
     *   "address": {
     *     "address": "address",
     *     "address_detail": "address_detail_1",
     *     "zip_code": "111"
     *   }
     * }
     * ```
     *
     * @return minify 이후 JSON
     * ```
     * {"name":"xxx","address":{"address":"address","address_detail":"address_detail_1","zip_code":"111"}}
     * ```
     *
     * @sample com.example.kotlincoroutine.JsonSupportTest.minifyJson
     */
    fun minifyJson(json: String): String {
        val result = StringBuilder()
        var insideString = false
        var escaped = false

        for (char in json) {
            when {
                escaped -> {
                    // 이스케이프된 문자는 그대로 추가
                    result.append(char)
                    escaped = false
                }
                char == '\\' -> {
                    // 이스케이프 문자 처리
                    result.append(char)
                    if (insideString) escaped = true
                }
                char == '"' -> {
                    // 따옴표 처리
                    insideString = !insideString
                    result.append(char)
                }
                insideString || !char.isWhitespace() -> {
                    // 문자열 안이거나 공백이 아닌 문자는 그대로 추가
                    result.append(char)
                }
            }
        }
        return result.toString()
    }
}
```

## Test Code

```kotlin
class JsonSupportTest {

    @Test
    fun minifyJson() {
        // given
        val json = """
            {
              "name": "xxx",
              "address": {
                "address": "address",
                "address_detail": "address_detail_1",
                "zip_code": "111"
              }
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(json)

        // then
        then(minifyJson).isEqualTo("{\"name\":\"xxx\",\"address\":{\"address\":\"address\",\"address_detail\":\"address_detail_1\",\"zip_code\":\"111\"}}")
    }

    @Test
    fun `복잡한 구조로 Json 축소 테스트`() {
        // given
        val complexJson = """
            {
                "user": {
                    "name": "John Doe",
                    "age": 30,
                    "address": {
                        "city": "New York",
                        "zip": "10001"
                    },
                    "favorites": ["Reading", "Coding", "Music"]
                },
                "status": "active",
                "timestamp": "2023-10-01T00:00:00Z"
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(complexJson)

        // then
        val expected = """{"user":{"name":"John Doe","age":30,"address":{"city":"New York","zip":"10001"},"favorites":["Reading","Coding","Music"]},"status":"active","timestamp":"2023-10-01T00:00:00Z"}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `중첩 배열로 Json 축소 테스트`() {
        // given
        val jsonWithArrays = """
            {
                "data": [
                    [1, 2, 3],
                    [4, 5, 6],
                    [7, 8, 9]
                ]
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithArrays)

        // then
        val expected = """{"data":[[1,2,3],[4,5,6],[7,8,9]]}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `특수 문자로 Json 축소 테스트`() {
        // given
        val jsonWithSpecialChars = """
            {
                "text": "This is a string with special characters: \t\n\r\b\f"
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithSpecialChars)

        // then
        val expected = """{"text":"This is a string with special characters: \t\n\r\b\f"}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `유니코드 문자로 Json 축소 테스트`() {
        // given
        val jsonWithUnicode = """
            {
                "message": "Hello, 世界"
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithUnicode)

        // then
        val expected = """{"message":"Hello, 世界"}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `테스트 Minify 빈 Json`() {
        // given
        val emptyJson = "{}"

        // when
        val minifyJson = JsonSupport.minifyJson(emptyJson)

        // then
        val expected = "{}"
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `Boolean 및 Null을 사용하여 Json 축소 테스트`() {
        // given
        val jsonWithBooleanAndNull = """
            {
                "isValid": true,
                "result": null
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithBooleanAndNull)

        // then
        val expected = """{"isValid":true,"result":null}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `단일 문자열 필드를 가진 Json 축소 테스트`() {
        // given
        val jsonWithSingleString = """
            {
                "greeting": "Hello, World!"
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithSingleString)

        // then
        val expected = """{"greeting":"Hello, World!"}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `복수의 숫자 필드를 가진 Json 축소 테스트`() {
        // given
        val jsonWithNumbers = """
            {
                "integer": 123,
                "float": 123.45,
                "negative": -67
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithNumbers)

        // then
        val expected = """{"integer":123,"float":123.45,"negative":-67}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `복잡한 배열 구조를 가진 Json 축소 테스트`() {
        // given
        val jsonWithComplexArrays = """
            {
                "numbers": [1, 2, 3, 4],
                "strings": ["one", "two", "three"],
                "mixed": [1, "two", 3.0, true]
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithComplexArrays)

        // then
        val expected = """{"numbers":[1,2,3,4],"strings":["one","two","three"],"mixed":[1,"two",3.0,true]}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `중첩된 객체를 가진 Json 축소 테스트`() {
        // given
        val jsonWithNestedObjects = """
            {
                "outer": {
                    "inner": {
                        "key": "value"
                    },
                    "anotherKey": 123
                }
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithNestedObjects)

        // then
        val expected = """{"outer":{"inner":{"key":"value"},"anotherKey":123}}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `null 값을 가진 Json 축소 테스트`() {
        // given
        val jsonWithNull = """
            {
                "key": null,
                "anotherKey": "value"
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithNull)

        // then
        val expected = """{"key":null,"anotherKey":"value"}"""
        then(minifyJson).isEqualTo(expected)
    }

    @Test
    fun `빈 배열을 가진 Json 축소 테스트`() {
        // given
        val jsonWithEmptyArray = """
            {
                "emptyArray": [],
                "key": "value"
            }
        """.trimIndent()

        // when
        val minifyJson = JsonSupport.minifyJson(jsonWithEmptyArray)

        // then
        val expected = """{"emptyArray":[],"key":"value"}"""
        then(minifyJson).isEqualTo(expected)
    }
}
```



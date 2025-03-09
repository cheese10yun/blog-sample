## $unwind

아래 표는 **\$unwind** 스테이지에서 사용할 수 있는 주요 옵션들을 요약한 것입니다.

| **옵션**                         | **타입** | **설명**                                                                                                                                                 | **기본값**           |
|--------------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| **path**                       | 문자열    | 펼칠 배열 필드의 경로를 지정합니다. 예: `"path": "$tags"`<br>이 옵션은 **반드시** 지정해야 하며, `"$"`를 포함한 점 표기법으로 필드를 표현합니다.                                                      | (필수)              |
| **preserveNullAndEmptyArrays** | 불리언    | `true`로 설정하면, `path`가 `null`이거나 빈 배열일 때에도 원본 문서를 그대로 출력합니다. `false`로 설정하면 해당 문서는 제거됩니다.<br>예: `"preserveNullAndEmptyArrays": true`                     | `false`           |
| **includeArrayIndex**          | 문자열    | 펼쳐진 배열 요소의 인덱스를 저장할 새 필드 이름을 지정합니다. 예: `"includeArrayIndex": "arrayIndex"`<br>이 옵션을 지정하면, 각 문서에 `"arrayIndex"` 필드가 추가되어 배열 내 현재 요소의 인덱스(0부터 시작)를 담습니다. | (지정하지 않으면 생성 안 함) |

- **예시**
  ```json
  {
    "$unwind": {
      "path": "$items",
      "preserveNullAndEmptyArrays": true,
      "includeArrayIndex": "itemIndex"
    }
  }
  ```
  위 예시에서는 `items` 필드(배열)를 펼치면서, `null`이나 빈 배열인 경우에도 문서를 유지하며, 펼쳐진 배열 요소의 인덱스를 `itemIndex`라는 필드에 저장합니다.

이처럼 **\$unwind** 스테이지는 배열 필드를 펼쳐서 여러 문서로 분할할 때 유용하며, 옵션을 통해 빈 배열/`null` 처리 방식과 인덱스 정보를 유연하게 관리할 수 있습니다.

## field-naming-strategy

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://root:example@localhost:27017/mongo_study?authSource=admin
      field-naming-strategy: org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy
```
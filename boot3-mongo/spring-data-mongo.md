다음은 각 필드의 의미와 용도를 정리한 테이블입니다.

| 필드             | 의미                        | 설명                                                                                                                     |
|----------------|---------------------------|------------------------------------------------------------------------------------------------------------------------|
| **name**       | 도메인 객체의 필드 이름             | 기본적으로 도메인 객체의 프로퍼티 이름이 MongoDB 도큐먼트의 필드 이름으로 사용됩니다.                                                                    |
| **nameType**   | 필드 이름의 해석/변환 방식           | 네이밍 전략을 적용할 때 사용되며, 특정 규칙이나 별칭 적용 등을 통해 필드 이름을 변환하는 데 참고될 수 있습니다.                                                      |
| **order**      | 필드 순서 지정                  | 도큐먼트에 필드를 저장할 때의 순서를 지정합니다. MongoDB에서는 필드 순서가 필수적이지 않지만, 직렬화나 가독성, 디버깅 목적에 활용될 수 있습니다.                                 |
| **value**      | MongoDB 도큐먼트의 실제 필드 이름 지정 | 도메인 객체의 필드명과 실제 저장될 필드명이 다를 경우, 원하는 필드명을 지정할 수 있습니다. (예: `userName` -> `username`)                                     |
| **write**      | 쓰기(write) 동작 포함 여부 결정     | 해당 필드가 MongoDB에 쓰기 작업 시 포함될지 여부를 결정합니다. 필요에 따라 읽기 전용으로 설정하거나, 계산된 값처럼 저장하지 않을 필드를 지정할 때 사용합니다.                         |
| **targetType** | 저장 시 변환할 BSON 타입 지정       | 도메인 객체의 필드 타입을 MongoDB에 저장할 때 명시적으로 지정할 BSON 타입입니다. 자동 변환 대신 특정 타입(예: `STRING`, `INT32`, `DATE` 등)으로 변환되도록 설정할 수 있습니다. |

이 테이블을 참고하여 포스팅에서 각 속성이 어떻게 사용되는지 구체적으로 설명하시면 도움이 될 것입니다.
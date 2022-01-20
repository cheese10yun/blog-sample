# Error Response 서버로 전달하기

[Spring Guide - Exception 전략](https://cheese10yun.github.io/spring-guide-exception/)이전에 API Server에 Exception handling에 대해서 정리한 적 있습니다. 이번 포스팅에서는 여러 서버를 호출해서 예외가 발생하는 경우에 대한 Exception handling  

더 구체적으로 설명드리면 A API -> B API -> C API 호출을 진행 하는 경우 C API에서 발생한 예외를 A API에게 그대로 전달 해야하는 경우가 있습니다. C Server에서 아래와 같은 예외가 발생했다고 가정해 보겠습니다. 

```json
{
  "message": " Invalid Input Value",
  "status": 400,
  "errors": [
    {
      "field": "name",
      "value": "",
      "reason": "must not be empty"
    },
    {
      "field": "email",
      "value": "",
      "reason": "must not be empty"
    }
  ],
  "code": "C001"
}
```
first, last name이 비여있어서 예외가 발생하고 있습니다. 해당 예외를 A Server에게 그대로 전달하지 않은 경우 정확하게 전달하는 것이 바람직합니다. **물론 A, B, C API 서버가 우리가 직접 개발하는 서버로 Error Response를 통일 했을 경우입니다.**







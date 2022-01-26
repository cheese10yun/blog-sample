# HTTP Client 책임 분리하기

일반적으로 특정 요구사항을 만족하기 위해서는 다른 서비스들과의 통신을 통하여 진행 합니다. 가장 대표적인 통신은 HTTP으로 쉽고 간결하게 연결이 가능하여 많이 사용 합니다. 

HTTP Client를 개발할 때 적절한 책임과 역할을 부여하는 방법에 대해서 이번 포스팅에서 다둘려고 합니다. 보편적으로 이렇게 책임과 역할을 나누는 것이 좋다는 것이지 모든 프로젝트에 알맞는 패턴이라고 주장하는 것은 아닙니다.


## HTTP Client Code

HTTP Client 라이브러리는 [fuel](https://github.com/kittinunf/fuel)를 사용했습니다. 특정 라이브러리에 대한 부분이 핵심이 아니기 때문에 다른 클라이언트를 사용하는 경우에도 무방합니다. 개인적으로 사용법이 직관적이고 코틀린 베이스이기 때문에 코틀린으로 프로젝트를 진행중이다면 [fuel](https://github.com/kittinunf/fuel)을 추천드립니다.


```kotlin
class BookClient(
    private val host: String = "http://localhost:8080"
) {

    fun getUser(bookId: Long) =
        "$host/api/v1/books/$bookId"
            .httpGet()
            .responseObject<Book>()
            .third
            .onError {
                if (it.response.isSuccessful.not()) {
                    throw IllegalArgumentException("bookId: $bookId not found")
                }
            }
            .get()


    fun updateBookStatus(
        status: String
    ) =
        "$host/api/v1/books"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                """
                    {
                      "status": "$status"
                    }
                """.trimIndent()
            )
            .response()
            .third
            .onError {
                if (it.response.isSuccessful.not()) {
                    throw IllegalArgumentException("Book Status Failed")
                }
            }
            .get()
}
```
위와 같은 방식으로 클라이언트를 작성하는 경우가 있습니다. 해당 코드의 문제는 무었일까요?


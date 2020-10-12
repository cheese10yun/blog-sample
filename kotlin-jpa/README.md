

# 정리 할 내용
* plugin.jpa 플러그인이 하는 일


## all-open
* JPA는 프록시 기반으로 동작하기 떄문에 상속을 위해서 open 키워드가 필요,
* 코틀린은 기본이 final
* 멤버 필드도 open이 필요, open이 없는 경우 레이지로딩시 프록시객체가 아니라 실체 객체가 리턴됨
* 자바 코드가 실제 어떻게 나오는지 보여줄것

## no-argument
* JPA에서 프록시 객체를 만들기 위해서 no-argument 생성자가 필요함
* 자바 코드가 실제 어떻게 나오는지 보여줄것

## Data와 JPA 엔티티 궁합
* 두가지 엔티티 궁합은 좋지 않다고 봄, 사용하지 않는 것을 개인적으로 권장
* equals, hashCode, toString 등의에서 문제 toString에서는 순한참조 문제가 발생할 소지가 있음

 

## 참고

* [JPA Kotlin](https://github.com/cheese10yun/blog-sample/blob/master/kotlin-cook-book/README.md)
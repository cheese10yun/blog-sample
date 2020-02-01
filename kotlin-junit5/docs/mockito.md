# Mockito
> [더 자바, 애플리케이션을 테스트하는 다양한 방법](https://www.inflearn.com/course/the-java-application-test/)을 보고 학습한 내용을 정리한 것입니다.

Mock: 진짜 객체와 비슷하게 동작하지만 프로그래머가 직접 그 객체의 행동을 관리하는 객체.
Mockito: Mock 객체를 쉽게 만들고 관리하고 검증할 수 있는 방법을 제공한다.


## Mock 객체 만들기
```kotlin
@ExtendWith(MockitoExtension::class)
internal class MemberServiceTest {

    @InjectMocks
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var memberRepository: MemberRepository
    
    @Test
    internal fun `mock test`() {
        //given
        val id = 1L
        val name = "new name"
        given(memberRepository.findById(id)).willReturn(Optional.of(Member("something@asd.com", name)))

        //when
        val member = memberService.updateName(name, id)
        
        //then
        println(member)
        then(member.name).isEqualTo(name)
    }
}
```
* @Mock 애노테이션으로 만드는 방법
  * JUnit 5 extension으로 MockitoExtension을 사용해야 한다.
  * 필드
  * 메소드 매개변수

## 객체 Stubbing
* 모든 Mock 객체의 행동
  * Null을 리턴한다. (Optional 타입은 Optional.empty 리턴)
  * Primitive 타입은 기본 Primitive 값.
  * 콜렉션은 비어있는 콜렉션.
  * Void 메소드는 예외를 던지지 않고 아무런 일도 발생하지 않는다.

* Mock 객체를 조작해서
  * 특정한 매개변수를 받은 경우 특정한 값을 리턴하거나 예뢰를 던지도록 만들 수 있다.
  * Void 메소드 특정 매개변수를 받거나 호출된 경우 예외를 발생 시킬 수 있다.
  * 메소드가 동일한 매개변수로 여러번 호출될 때 각기 다르게 행동호도록 조작할 수도 있다.

```kotlin
@Test
internal fun `member service mock test`() {

    //given
    val name = "new name"
    given(memberRepository.findById(anyLong())).willReturn(Optional.empty())

    //when & then
    thenThrownBy {
        memberService.updateName(name, 1)
    }
        .isExactlyInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("is not existed")
}
```


```kotlin
@Test
internal fun `given절을 여러번 호출을 할 수 있다`() {
    //given
    val name = "new_name"
    given(memberRepository.findById(anyLong()))
        .willReturn(Optional.empty())
        .willReturn(Optional.of(Member("something@asd.com", name)))

    //when

    thenThrownBy {
        memberService.updateName(name, 1)
    }
        .isExactlyInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("is not existed")


    val member = memberService.updateName(name, 1)

    //then
    then(member.name).isEqualTo(name)
}
```
* `given`절에서 체이닝해서 여러 `given`을 만들 수 있으며 순서대로 `when`, `then`을 구현 하면된다.

## Mock 객체 확인
Mock 객체가 어떻게 사용이 됐는지 확인할 수 있다.

* 특정 메소드가 특정 매개변수로 몇번 호출 되었는지, 최소 한번은 호출 됐는지, 전혀 호출되지 않았는지
* 어떤 순서대로 호출했는지
* 특정 시간 이내에 호출됐는지
* 특정 시점 이후에 아무 일도 벌어지지 않았는지

```kotlin
@Service
class MemberService(
    private val memberRepository: MemberRepository
) {

    fun updateName(name: String, id: Long): Member {
        val member = memberRepository.findById(id).orElseThrow { IllegalArgumentException("$id is not existed") }
        memberRepository.findByName(name)
        member.updateName(name)
        return member
    }
}

@Test
internal fun `verify test`() {
    //given
    val name = "new name"
    given(memberRepository.findById(anyLong())).willReturn(Optional.of(Member("something@asd.com", name)))

    //when
    val member = memberService.updateName(name, 1)

    //then
    println(member)
    then(member.name).isEqualTo(name)
    verify(memberRepository, times(1)).findById(ArgumentMatchers.anyLong())
    verify(memberRepository, times(1)).findByName(name)
    verify(memberRepository, never()).save(any())
}
```

* verify 으로 검증할 수있음
  * findById, findByName 1번 호출 되어야함
  * save 호출하면 안된다.

## BDD 스타일 API

BDD : 애플리케이션이 어떻게 “행동”해야 하는지에 대한 공통된 이해를 구성하는 방법으로, TDD에서 창안했다. Mockito는 BddMockito라는 클래스를 통해 BDD 스타일의 API를 제공한다.

When -> Given
Verify -> Then

```kotlin
@Test
internal fun `verify test`() {
    //given
    val name = "new name"
    given(memberRepository.findById(anyLong())).willReturn(Optional.of(Member("something@asd.com", name)))

    //when
    val member = memberService.updateName(name, 1)

    //then
    println(member)
    then(memberRepository).should(times((1))).findById(anyLong())
    then(memberRepository).should(times((1))).findByName(name)
    then(memberRepository).should(never()).save(any())
}
```

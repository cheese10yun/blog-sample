## val 으로 불변을 보장 받고 싶을 때

val 으로 불변을 보장 받으면 코드의 예측, 유지보수 측면에서 많은 장점들이 있어 가능하면 val 으로 사용하기 위해 많은 노력들을 한다. 예를 들어 AdultMember를 조회하여 이메일 인증을 진행하지 않은 경우 상태를 UNVERIFIED("미인증")으로 변경해야 하는 경우를 생각해보자.

```kotlin
data class AdultMember(
    // ...
    val id: Long,
    override val email: String,
    override val firstName: String,
    override val lastName: String,
    var status: MemberStatus, // var 선언
) : GeneralMember

```
이메일 인증여부를 검사하고 인증되지 않은 AdultMember의 status 필드를 변경 하려면 변수를 var으로 선언 해야한다.

```kotlin
class MemberStatusManagementService {
    fun getUnverifiedMembers(adultMembers: List<AdultMember>): List<AdultMember> {
        return adultMembers.mapNotNull {
            // 이메일 미인증 여부를 로직 ...
            if (true) {
                // 필드 변경
                it.apply { this.status = MemberStatus.UNVERIFIED }
            } else {
                null
            }
        }
    }
}
```
var 으로 선언한 status 필드를 직접 정의해서 업데이트를 진행하고 미인증 AdultMember 객체를 리턴 해주고 있다. 이런 케이스의 경우에는 data class의 copy를 사용하는 것이 효율적이다.


```kotlin
class MemberStatusManagementService {
    fun getUnverifiedMembers(adultMembers: List<AdultMember>): List<AdultMember> {
        return adultMembers.mapNotNull {
            // 이메일 미인증 여부를 로직 ...
            if (true) {
                it.copy(status = MemberStatus.UNVERIFIED)
            } else {
                null
            }
        }
    }
}
```
AdultMember 객체의 status을 val으로 변경하고 실제 데이터 변경은 copy통해 진행한다. 
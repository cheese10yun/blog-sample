package com.spring.camp.domain

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.minSizeExp
import com.navercorp.fixturemonkey.kotlin.setExp
import java.lang.reflect.Field
import java.time.Instant
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class FixtureMonkey {
    var sut: FixtureMonkey = FixtureMonkey.builder()

        .build()

    private val fixtureMonkey = FixtureMonkey
        .builder()
        .plugin(KotlinPlugin())
        .objectIntrospector(
            FailoverIntrospector(
                listOf(
//                    FieldReflectionArbitraryIntrospector.INSTANCE,
                    ConstructorPropertiesArbitraryIntrospector.INSTANCE
                )
            )
        )

        .build()
    @Test
    fun ㅁㄴㅇㅁㄴㅇ() {
        println("")
    }

    @Test
    fun `test with fixture monkey and custom email`() {
        // Person 객체 생성 시 email을 특정 값으로 지정
        val person: Person = fixtureMonkey.giveMeBuilder<Person>()
            .setExp(Person::email, "asdar@example.com")
            .setExp(Person::name, "asㅁㄴㅇ")
            .setExp(Person::age, 10)
            .setExp(Person::state, "DDD")
            .sample()

        println(person) // 생성된 Person 객체 출력
    }

    @Test
    fun `test with fixture monkey and reflection`() {
        val person: Person = fixtureMonkey.giveMeOne<Person>()

        // 리플렉션을 사용하여 email 필드 수정
        val emailField: Field = person::class.java.getDeclaredField("email")
        emailField.isAccessible = true
        emailField.set(person, "asdasd@example.com")

        println(person) // 수정된 email 필드를 포함한 Person 객체 출력
    }


    @Test
    fun sampleOrder() {
        // given


        // when
        val actual = fixtureMonkey.giveMeBuilder<OrderFixture>()
            .setExp(OrderFixture::orderNo, "1")
            .setExp(OrderFixture::productName, "Line Sally")
            .minSizeExp(OrderFixture::items, 1)
            .sample()

        // then
        println(actual)
        then(actual.orderNo).isEqualTo("1")
        then(actual.productName).isEqualTo("Line Sally")
        then(actual.items).hasSizeGreaterThanOrEqualTo(1)
    }

}

data class OrderFixture (
    val id: Long,
    val orderNo: String,
    val productName: String,
    val quantity: Int,
    val price: Long,
    val items: List<String>,
    val orderedAt: Instant
)


class Person(
    name: String,
    age: Int
) {
    val email: String = "$name@asd.com"
    val name: String = name
    val age: Int = age
    val state = "active"
    override fun toString(): String {
        return "Person(email='$email', name='$name', age=$age, state='$state')"
    }
}

class MyServiceTest {

    private val fixtureMonkey = FixtureMonkey
        .builder()
        .plugin(KotlinPlugin())
        .build()


}
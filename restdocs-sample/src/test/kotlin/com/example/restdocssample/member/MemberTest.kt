package com.example.restdocssample.member

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.minSizeExp
import com.navercorp.fixturemonkey.kotlin.setExp
import org.junit.jupiter.api.Test


class MemberTest{

    @Test
    fun `asd`() {

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

        val actual = sut.giveMeBuilder<Member>()
            .setExp(Member::id, 1L)
            .setExp(Member::email, "Line Sally")
            .setExp(Member::name, "Line Sally")
//            .setExp(Member::asd, "Line Sally")
            .sample()!!

        val member: Member = sut.giveMeOne()

        println(member)
    }
}
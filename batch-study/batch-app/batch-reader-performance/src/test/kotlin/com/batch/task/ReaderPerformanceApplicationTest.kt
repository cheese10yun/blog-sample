package com.batch.task

import com.batch.payment.domain.core.EntityAuditing
import com.batch.payment.domain.payment.Payment
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses


internal class ReaderPerformanceApplicationTest {


    @Test
    fun name() {
//        val field: Field = Payment..getDeclaredField("id")


        val fields = Payment::class.declaredMemberProperties


        val superclasses = Payment::class.superclasses

        if (superclasses.isEmpty()) {
            Payment::class.declaredMemberProperties
        } else {
            val superclasses = Payment::class.superclasses

            for (suerClass in superclasses) {

                println("=========")
                println(suerClass.declaredMemberProperties)

                suerClass.members

                val declaredField = Class.forName(EntityAuditing::class.qualifiedName).getDeclaredField("id")

                val get = declaredField.get(Payment::class)







                println()

                for (propertie in  suerClass.declaredMemberProperties){

                }


            }
        }

    }

    @Test
    fun asdasdasd() {


    }

}
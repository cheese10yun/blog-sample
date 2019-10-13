package com.example.springkotlin.global

import com.example.springkotlin.domain.transaction.PaymentMethodType
import com.example.springkotlin.domain.transaction.Transaction
import com.example.springkotlin.domain.transaction.TransactionRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.util.*


@Component
class AppRunner(val transactionRepository: TransactionRepository) : ApplicationRunner {



    override fun run(args: ApplicationArguments?) {
        val stopWatch = StopWatch("test")
        val transactions = ArrayList<Transaction>()

        stopWatch.start("Transaction")
        for (i in 1..10000){

            transactions.add(Transaction.newInstance(
                    code = UUID.randomUUID().toString(),
                    paymentMethodType = random(),
                    thirdPartyTransactionId = UUID.randomUUID().toString()
            ))
        }

        stopWatch.stop()


        transactionRepository.saveAll(transactions)

        println(stopWatch.shortSummary())
        println(stopWatch.getTotalTimeMillis())
        println(stopWatch.prettyPrint())
    }

    fun random(): PaymentMethodType {
        return  when (Random().nextInt(4)) {
            1 -> PaymentMethodType.CARD
            2 -> PaymentMethodType.CASH
            3 -> PaymentMethodType.KAKAOPAY
            else -> PaymentMethodType.TOSS
        }
    }
}
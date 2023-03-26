package com.example.querydsl.service

import com.example.querydsl.domain.Team
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ApiService(
    private val restTemplate: RestTemplate,
) {

    fun getTeam(name: String): List<Team> {
        return restTemplate.getForObject("/teams?name=$name", Array<Team>::class.java)!!.toList()
    }
}

@Service
class OrderService(
    private val exchangeRateObtainService: ExchangeRateObtainService,
) {

    /**
     *
     */
    fun order(form: OrderForm) {
        val exchangeRateResponse = exchangeRateObtainService.obtainExchangeRate(
            date = LocalDate.now(),
            form = "USD",
            to = "KRW"
        )
    }

}

@Service
class ExchangeRateObtainService(
    private val restTemplate: RestTemplate,
) {

    fun obtainExchangeRate(
        date: LocalDate,
        form: String,
        to: String,
    ): ExchangeRateResponse {
        return restTemplate.getForObject("/api/v1/exchange-rate?date=${date}&form=${form}&to=${to}", ExchangeRateResponse::class.java)!!
    }
}

data class ExchangeRateResponse(
    val exchangeRate: BigDecimal,
)

data class OrderForm(
    val price: BigDecimal,
)
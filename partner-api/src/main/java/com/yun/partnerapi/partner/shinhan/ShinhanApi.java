package com.yun.partnerapi.partner.shinhan;

import com.yun.partnerapi.partner.PartnerManagerDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
public class ShinhanApi {

    private final RestTemplate restTemplate;
    private final HttpEntity httpEntity = getHttpEntityInstance();

    public ShinhanDto.ExchangeRateResponse getExchangeRate(PartnerManagerDto.ExchangeCurrency exchangeCurrency) {
        final String URL = "신한은행 Exchange Rate 정보";
        final ResponseEntity<ShinhanDto.ExchangeRateResponse> response = restTemplate.exchange(URL, HttpMethod.GET, httpEntity, ShinhanDto.ExchangeRateResponse.class);
        return response.getBody();
    }


    private HttpEntity getHttpEntityInstance() {
        return new HttpEntity(getHttpHeadersInstance());
    }

    private HttpHeaders getHttpHeadersInstance() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("SECRET", "value");
        headers.add("CLIENT", "value");
        return headers;
    }

}

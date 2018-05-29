package com.yun.partnerapi.partner.shinhan;

import com.yun.partnerapi.partner.PartnerExchangeRate;
import com.yun.partnerapi.partner.PartnerManagerDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ShinhanExchangeRate implements PartnerExchangeRate {

    private final ShinhanApi shinhanApi;

    @Override
    public PartnerManagerDto.ExchangeRate get(PartnerManagerDto.ExchangeCurrency dto) {
        final ShinhanDto.ExchangeRateResponse response = shinhanApi.getExchangeRate(dto);
        return PartnerManagerDto.ExchangeRate.builder()
                .rate(response.getFxRate())
                .exchangeCurrency(dto)
                .build();
    }

}

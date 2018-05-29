package com.yun.partnerapi.partner.woori;

import com.yun.partnerapi.partner.PartnerExchangeRate;
import com.yun.partnerapi.partner.PartnerManagerDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WooriExchangeRate implements PartnerExchangeRate {

    private final WooriApi wooriApi;

    @Override
    public PartnerManagerDto.ExchangeRate get(PartnerManagerDto.ExchangeCurrency dto) {
        final WooriDto.ExchangeRateResponse response = wooriApi.getExchangeRate(dto);

        return PartnerManagerDto.ExchangeRate.builder()
                .rate(response.getExchangeRate())
                .exchangeCurrency(dto)
                .build();

    }
}

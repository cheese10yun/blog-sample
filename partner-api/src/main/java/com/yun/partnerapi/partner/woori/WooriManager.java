package com.yun.partnerapi.partner.woori;

import com.yun.partnerapi.partner.PartnerManager;
import com.yun.partnerapi.partner.PartnerManagerDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WooriManager implements PartnerManager {

    private final WooriApi wooriApi;

    @Override
    public PartnerManagerDto.ExchangeRate getExchangeRate(PartnerManagerDto.ExchangeCurrency dto) {
        final WooriDto.ExchangeRateResponse response = wooriApi.getExchangeRate(dto);

        return PartnerManagerDto.ExchangeRate.builder()
                .rate(response.getExchangeRate())
                .exchangeCurrency(dto)
                .build();

    }
}

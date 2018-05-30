package com.yun.partnerapi.anti;

import com.yun.partnerapi.partner.PartnerManagerDto;

public class JbApi implements  PartnerApi{
    @Override
    public PartnerManagerDto.ExchangeRate getExchangeRate(PartnerManagerDto.ExchangeCurrency dstCurrency) {
        return null;
    }

    @Override
    public PartnerManagerDto.Banks getBanks(PartnerManagerDto.ExchangeCurrency dto) {
        return null;
    }
}

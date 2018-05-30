package com.yun.partnerapi.anti;

import com.yun.partnerapi.partner.PartnerManagerDto;

public interface PartnerApi {

    PartnerManagerDto.ExchangeRate getExchangeRate(PartnerManagerDto.ExchangeCurrency dstCurrency);

    PartnerManagerDto.Banks getBanks(PartnerManagerDto.ExchangeCurrency dto);

}

package com.yun.partnerapi.anti;

import com.yun.partnerapi.partner.PartnerManagerDto;

public interface PartnerApi {
    PartnerManagerDto.ExchangeRate get(PartnerManagerDto.ExchangeCurrency dstCurrency);
}

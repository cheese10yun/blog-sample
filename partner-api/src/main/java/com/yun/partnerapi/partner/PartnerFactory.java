package com.yun.partnerapi.partner;

import com.yun.partnerapi.model.Currency;
import com.yun.partnerapi.partner.shinhan.ShinhanExchangeRate;
import com.yun.partnerapi.partner.woori.WooriExchangeRate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PartnerFactory {

    private final ShinhanExchangeRate shinhanExchangeRate;
    private final WooriExchangeRate wooriExchangeRate;

    public PartnerExchangeRate getInstance(final Currency dstCurrency) {
        final PartnerExchangeRate partnerExchangeRate;

        switch (dstCurrency) {
            case KRW:
            case VND:
                partnerExchangeRate = shinhanExchangeRate;
                break;
            case USD:
                partnerExchangeRate = wooriExchangeRate;
                break;
            default:
                throw new IllegalArgumentException(dstCurrency.name() + " is not Found");
        }
        return partnerExchangeRate;
    }
}

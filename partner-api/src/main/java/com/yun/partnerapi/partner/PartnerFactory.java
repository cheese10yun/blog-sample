package com.yun.partnerapi.partner;

import com.yun.partnerapi.model.Currency;
import com.yun.partnerapi.partner.shinhan.ShinhanManager;
import com.yun.partnerapi.partner.woori.WooriManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PartnerFactory {

    private final ShinhanManager shinhanManager;
    private final WooriManager wooriManager;

    public PartnerManager getInstance(final Currency dstCurrency) {
        final PartnerManager partnerManager;

        switch (dstCurrency) {
            case KRW:
            case VND:
                partnerManager = shinhanManager;
                break;
            case USD:
                partnerManager = wooriManager;
                break;
            default:
                throw new IllegalArgumentException(dstCurrency.name() + " is not Found");
        }
        return partnerManager;
    }
}

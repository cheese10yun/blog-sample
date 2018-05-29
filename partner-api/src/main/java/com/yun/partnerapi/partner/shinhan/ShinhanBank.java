package com.yun.partnerapi.partner.shinhan;

import com.yun.partnerapi.partner.PartnerBank;
import com.yun.partnerapi.partner.PartnerManagerDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ShinhanBank implements PartnerBank {



    @Override
    public PartnerManagerDto.Banks get() {
        return null;
    }
}

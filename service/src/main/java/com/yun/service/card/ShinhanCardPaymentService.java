package com.yun.service.card;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ShinhanCardPaymentService implements CardPaymentService {

    private ShinhanCardPaymentService shinhanCard;

    @Override
    public void pay() {
        shinhanCard.pay(); //신한 카드 결제 API 호출
        // 결제를 위한 비즈니스 로직 실행....
    }

    @Override
    public void cancel() {

    }
}

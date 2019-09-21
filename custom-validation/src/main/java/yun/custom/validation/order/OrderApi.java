package yun.custom.validation.order;


import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderApi {

    @PostMapping
    public OrderSheetRequest order(@RequestBody @Valid final OrderSheetRequest dto) {

        if (dto.getPayment().getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            // 계좌정보가 제대로 넘어 왔는지?
        }

        if((dto.getPayment().getPaymentMethod() == PaymentMethod.CARD)){
            // 카드 정보 제대로 넘어 왔는지?
        }

        return dto;
    }
}

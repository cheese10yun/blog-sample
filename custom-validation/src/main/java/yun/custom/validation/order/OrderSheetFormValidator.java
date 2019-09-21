package yun.custom.validation.order;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import yun.custom.validation.order.OrderSheetRequest.Account;
import yun.custom.validation.order.OrderSheetRequest.Card;

public class OrderSheetFormValidator implements ConstraintValidator<OrderSheetForm, OrderSheetRequest> {


    @Override
    public void initialize(OrderSheetForm constraintAnnotation) {

    }

    @Override
    public boolean isValid(OrderSheetRequest value, ConstraintValidatorContext context) {

        int invalidCount = 0;

        if (value.getPayment().hasPaymentInfo()) {
            addConstraintViolation(context, "카드 정보 혹은 계좌정보는 필수입니다.", "payment");
            invalidCount += 1;
        }

        if (value.getPayment().getPaymentMethod() == PaymentMethod.CARD) {
            final Card card = value.getPayment().getCard();

            if (card == null) {
                addConstraintViolation(context, "카드 필수입니다.", "payment", "card");

            } else {
                if (StringUtils.isEmpty(card.getBrand())) {
                    addConstraintViolation(context, "카드 브렌드는 필수입니다.", "payment", "card", "brand");
                    invalidCount += 1;
                }

                if (StringUtils.isEmpty(card.getCsv())) {
                    addConstraintViolation(context, "CSV 값은 필수 입니다.", "payment", "card", "csv");
                    invalidCount += 1;
                }

                if (StringUtils.isEmpty(card.getNumber())) {
                    addConstraintViolation(context, "카드 번호는 필수 입니다.", "payment", "card", "number");
                    invalidCount += 1;
                }
            }

        }

        if (value.getPayment().getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            final Account account = value.getPayment().getAccount();

            if (account == null) {
                addConstraintViolation(context, "계좌정보는 필수입니다.", "payment", "account");
                invalidCount += 1;
            } else {
                if (StringUtils.isEmpty(account.getBankCode())) {
                    addConstraintViolation(context, "은행코드는 필수입니다.", "payment", "account", "bankCode");
                    invalidCount += 1;
                }

                if (StringUtils.isEmpty(account.getHolder())) {
                    addConstraintViolation(context, "계좌주는 값은 필수 입니다.", "payment", "account", "holder");
                    invalidCount += 1;
                }

                if (StringUtils.isEmpty(account.getNumber())) {
                    addConstraintViolation(context, "계좌번호는 필수값입니다.", "payment", "account", "number");
                    invalidCount += 1;
                }

            }

        }

        return invalidCount == 0;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage,
        String firstNode, String secondNode, String thirdNode) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errorMessage)
            .addPropertyNode(firstNode)
            .addPropertyNode(secondNode)
            .addPropertyNode(thirdNode)
            .addConstraintViolation();
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage,
        String firstNode) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errorMessage)
            .addPropertyNode(firstNode)
            .addConstraintViolation();
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String errorMessage,
        String firstNode,
        String secondNode) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errorMessage)
            .addPropertyNode(firstNode)
            .addPropertyNode(secondNode)
            .addConstraintViolation();
    }
}
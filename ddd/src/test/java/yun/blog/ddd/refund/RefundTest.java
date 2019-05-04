package yun.blog.ddd.refund;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import yun.blog.ddd.model.Account;
import yun.blog.ddd.model.CreditCard;
import yun.blog.ddd.order.Order;
import yun.blog.ddd.order.OrderBuilder;

public class RefundTest {

  private Order order;
  private Account account;
  private CreditCard creditCard;

  @Before
  public void setUp() throws Exception {
    order = OrderBuilder.build();

    account = Account.builder()
        .accountHolder("홍길동")
        .accountNumber("110-2304-22344")
        .bankName("신한은행")
        .build();

    creditCard = CreditCard.builder()
        .creditNumber("110-22345-22345")
        .creditHolder("홍길동")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void ByAccountBuilder_test_account_null이면_excpetion() {

    Refund.ByAccountBuilder()
        .account(null)
        .order(order)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void ByAccountBuilder_testorder_null이면_excpetion() {
    Refund.ByAccountBuilder()
        .account(account)
        .order(null)
        .build();
  }

  @Test
  public void ByAccountBuilder_test() {
    final Refund refund = Refund.ByAccountBuilder()
        .account(account)
        .order(order)
        .build();

    assertThat(refund.getAccount()).isEqualTo(account);
    assertThat(refund.getOrder()).isEqualTo(order);
  }


  @Test(expected = IllegalArgumentException.class)
  public void ByCreditBuilder_test_account_null이면_excpetion() {

    Refund.ByCreditBuilder()
        .creditCard(null)
        .order(order)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void ByCreditBuilder_testorder_null이면_excpetion() {
    Refund.ByCreditBuilder()
        .creditCard(creditCard)
        .order(null)
        .build();
  }

  @Test
  public void ByCreditBuilder_test() {
    final Refund refund = Refund.ByCreditBuilder()
        .creditCard(creditCard)
        .order(order)
        .build();

    assertThat(refund.getCreditCard()).isEqualTo(creditCard);
    assertThat(refund.getOrder()).isEqualTo(order);
  }


}
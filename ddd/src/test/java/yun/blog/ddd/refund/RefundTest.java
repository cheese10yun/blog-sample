package yun.blog.ddd.refund;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import yun.blog.ddd.model.Account;

public class RefundTest {


  @Test(expected = IllegalArgumentException.class)
  public void account_null이면_excpetion() {

    Refund.ByAccountBuilder()
        .account(null)
        .build();
  }

  @Test
  public void RefundTest() {
    final Account account = Account.builder()
        .accountHolder("홍길동")
        .accountNumber("110-2304-22344")
        .bankName("신한은행")
        .build();

    final Refund refund = Refund.ByAccountBuilder()
        .account(account)
        .build();

    assertThat(refund.getAccount()).isEqualTo(account);


  }
}
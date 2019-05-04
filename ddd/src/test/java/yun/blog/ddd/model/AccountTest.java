package yun.blog.ddd.model;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class AccountTest {

  @Test(expected = IllegalArgumentException.class)
  public void Account_accountHolder_비어있으면_exception() {
    Account.builder()
        .accountHolder("")
        .accountNumber("110-22345-22345")
        .bankName("신한은행")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Account_accountNumber_비어있으면_exception() {
    Account.builder()
        .accountHolder("홍길동")
        .accountNumber("")
        .bankName("신한은행")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Account_bankName_비어있으면_exception() {
    Account.builder()
        .accountHolder("홍길동")
        .accountNumber("110-22345-22345")
        .bankName("")
        .build();
  }

  @Test
  public void Account_test() {

    final Account address = Account.builder()
        .accountHolder("홍길동")
        .accountNumber("110-22345-22345")
        .bankName("신한은행")
        .build();

    assertThat(address.getAccountHolder()).isEqualTo("홍길동");
    assertThat(address.getAccountNumber()).isEqualTo("110-22345-22345");
    assertThat(address.getBankName()).isEqualTo("신한은행");

  }

}
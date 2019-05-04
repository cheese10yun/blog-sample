package yun.blog.ddd.model;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class CreditCardTest {

  @Test(expected = IllegalArgumentException.class)
  public void Account_creditNumber_비어있으면_exception() {
    CreditCard.builder()
        .creditNumber("")
        .creditHolder("홍길동")
        .build();
  }


  @Test(expected = IllegalArgumentException.class)
  public void Account_creditHolder_비어있으면_exception() {
    CreditCard.builder()
        .creditNumber("10-22345-22345")
        .creditHolder("")
        .build();
  }

  @Test
  public void Account_test() {
    final CreditCard address = CreditCard.builder()
        .creditNumber("110-22345-22345")
        .creditHolder("홍길동")
        .build();

    assertThat(address.getCreditHolder()).isEqualTo("홍길동");
    assertThat(address.getCreditNumber()).isEqualTo("110-22345-22345");
  }

}
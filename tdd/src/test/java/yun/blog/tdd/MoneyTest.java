package yun.blog.tdd;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class MoneyTest {

  @Test
  public void testMultiplication() {

    Dollar five = new Dollar(5);
    five.times(2);

    assertThat(five.getAmount()).isEqualTo(10);

  }
}

package yun.blog.tdd;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class MoneyTest {

//  @Test
//  public void testMultiplication_01() {
//
//    Dollar five = new Dollar(5);
//    five.times(2);
//
//    assertThat(five.getAmount()).isEqualTo(10);
//
//  }

  @Test
  public void testMultiplication_02() {

    Dollar five = new Dollar(5);

    Dollar product = five.times(2);


    assertThat(product.getAmount()).isEqualTo(10);


    product = five.times(3);
    assertThat(product.getAmount()).isEqualTo(15);


  }
}

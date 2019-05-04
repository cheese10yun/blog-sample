package yun.blog.ddd.product;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class ProductTest {

  @Test(expected = IllegalArgumentException.class)
  public void Product_price_0인경우_exception() {
    Product.builder()
        .price(0)
        .name("제품...")
        .build();
  }


  @Test(expected = IllegalArgumentException.class)
  public void Product_creditHolder_비어있으면_exception() {
    Product.builder()
        .price(1)
        .name("")
        .build();
  }

  @Test
  public void Product_test() {
    final Product product = Product.builder()
        .price(1)
        .name("제품...")
        .build();

    assertThat(product.getPrice()).isEqualTo(1);
    assertThat(product.getName()).isEqualTo("제품...");
  }


}
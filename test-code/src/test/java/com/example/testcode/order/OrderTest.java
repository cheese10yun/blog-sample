package com.example.testcode.order;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.example.testcode.product.Product;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class OrderTest {

  @Test
  public void 단수구매() {
    final Product product = new Product("양말");
    final Order order = Order.order("yun", product);

    assertThat(order).isNotNull();
    assertThat(order.getProducts()).contains(product);
    assertThat(order.getProducts().size()).isEqualTo(1);
  }

  @Test
  public void 복수구매() {

    final List<Product> products = new ArrayList<>();
    products.add(new Product("양말"));
    products.add(new Product("모자"));
    products.add(new Product("바지"));

    final Order order = Order.order("yun", products);

    assertThat(order).isNotNull();
    assertThat(order.getProducts().size()).isEqualTo(3);
  }
}
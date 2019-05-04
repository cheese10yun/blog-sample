package yun.blog.ddd.order;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import yun.blog.ddd.model.Address;
import yun.blog.ddd.product.Product;

public class OrderTest {

  private List<Product> products;

  @Before
  public void setUp() throws Exception {
    products = new ArrayList<>();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Order_address_null이면_exception() {
    final Product product = Product.builder()
        .price(1)
        .name("제품...")
        .build();

    products.add(product);

    final Order order = Order.builder()
        .address(null)
        .products(products)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void Order_products_비어있으면_exception() {

    final Address address = Address.builder()
        .address1("address 1")
        .address2("address 2")
        .zip("zip")
        .build();

    final Order order = Order.builder()
        .address(address)
        .products(products)
        .build();
  }


  @Test(expected = IllegalArgumentException.class)
  public void Order_products_null이면_exception() {

    final Address address = Address.builder()
        .address1("address 1")
        .address2("address 2")
        .zip("zip")
        .build();

    final Order order = Order.builder()
        .address(address)
        .products(null)
        .build();
  }


  @Test
  public void order_test() {

    final Address address = Address.builder()
        .address1("address 1")
        .address2("address 2")
        .zip("zip")
        .build();

    final Product product = Product.builder()
        .price(1)
        .name("제품...")
        .build();

    products.add(product);

    final Order order = Order.builder()
        .address(address)
        .products(products)
        .build();

    assertThat(order.getAddress()).isEqualTo(address);
    assertThat(order.getProducts()).contains(product);

  }
}
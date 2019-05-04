package yun.blog.ddd.order;

import java.util.ArrayList;
import java.util.List;
import yun.blog.ddd.model.Address;
import yun.blog.ddd.product.Product;

public class OrderBuilder {

  public static Order build() {

    final Address address = Address.builder()
        .address1("서울시 관악구 293-1")
        .address2("201호")
        .zip("503-23")
        .build();

    final List<Product> products = new ArrayList<>();

    final Product product = Product.builder()
        .price(1000)
        .name("제품...")
        .build();

    products.add(product);

    return Order.builder()
        .address(address)
        .products(products)
        .build();
  }

  public static Order build(Address address, List<Product> products) {

    return Order.builder()
        .address(address)
        .products(products)
        .build();
  }


}

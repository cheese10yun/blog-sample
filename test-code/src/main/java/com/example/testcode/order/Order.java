package com.example.testcode.order;


import com.example.testcode.product.Product;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "orderer", nullable = false)
  private String orderer;

  @Enumerated(value = EnumType.STRING)
  private OrderStep step;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<Product> products = new ArrayList<>();

  public static Order order(final String orderer, final Product product) {
    return new Order(orderer, product);
  }

  public static Order order(final String orderer, final List<Product> products) {
    return new Order(orderer, products);
  }

  public void changeStepToCompleted() {

    if (this.step != OrderStep.SHIPPING) {
      throw new IllegalStateException();
    }

    this.step = OrderStep.COMPLETED;
  }


  private Order(final String orderer, final List<Product> products) {
    this.orderer = orderer;
    this.products = products;

    for (final Product product : products) {
      product.applyOrder(this);
    }

  }

  private Order(final String orderer, final Product product) {
    this.orderer = orderer;
    this.products.add(product);
    product.applyOrder(this);
  }


}

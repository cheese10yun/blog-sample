package yun.blog.ddd.order;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import yun.blog.ddd.model.Address;
import yun.blog.ddd.product.Product;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private Address address;

  @OneToMany(mappedBy = "order")
  private List<Product> products = new ArrayList<>();

  @Builder
  public Order(Address address, List<Product> products) {
    Assert.notNull(address, "address must not be null");
    Assert.notNull(products, "products must not be null");
    Assert.notEmpty(products, "products must not be empty");

    this.address = address;
    this.products = products;
  }
}

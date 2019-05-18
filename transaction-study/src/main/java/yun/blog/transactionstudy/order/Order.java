package yun.blog.transactionstudy.order;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
@Getter
public class Order {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;


  @Column(name = "status", nullable = false)
  @Enumerated(value = EnumType.STRING)
  private OrderStatus status;

  public Order(OrderStatus status) {
    this.status = status;
  }
}

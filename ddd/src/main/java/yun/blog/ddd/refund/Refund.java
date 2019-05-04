package yun.blog.ddd.refund;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import yun.blog.ddd.model.Account;
import yun.blog.ddd.model.CreditCard;
import yun.blog.ddd.order.Order;

@Entity
@Table(name = "refund")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Embedded
  private Account account;

  @Embedded
  private CreditCard creditCard;

  @OneToOne
  @JoinColumn(name = "order_id", nullable = false, updatable = false)
  private Order order;


  @Builder(builderClassName = "ByAccountBuilder", builderMethodName = "ByAccountBuilder")
  public Refund(Account account, Order order) {
    Assert.notNull(account, "account must not be null");
    Assert.notNull(order, "account must not be null");

    this.order = order;
    this.account = account;
  }

  @Builder(builderClassName = "ByCreditBuilder", builderMethodName = "ByCreditBuilder")
  public Refund(CreditCard creditCard, Order order) {
    Assert.notNull(creditCard, "creditCard must not be null");
    Assert.notNull(order, "order must not be null");

    this.order = order;
    this.creditCard = creditCard;
  }
}

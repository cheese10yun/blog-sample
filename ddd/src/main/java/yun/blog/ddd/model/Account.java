package yun.blog.ddd.model;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

  @NotEmpty
  @Column(name = "bank_name", nullable = false)
  private String bankName;

  @NotEmpty
  @Column(name = "account_number", nullable = false)
  private String accountNumber;

  @NotEmpty
  @Column(name = "account_holder", nullable = false)
  private String accountHolder;

  @Builder
  public Account(String bankName, String accountNumber, String accountHolder) {
    Assert.hasText(bankName, "bankName must not be empty");
    Assert.hasText(accountNumber, "accountNumber must not be empty");
    Assert.hasText(accountHolder, "accountHolder must not be empty");

    this.bankName = bankName;
    this.accountNumber = accountNumber;
    this.accountHolder = accountHolder;
  }
}

package yun.blog.tdd;

import java.util.Objects;
import lombok.Getter;

@Getter
public class Dollar {

  private int amount;


  public Dollar(int amount) {
    this.amount = amount;
  }

  public Dollar times(int multiplier) {
    return new Dollar(amount * multiplier);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Dollar)) {
      return false;
    }
    Dollar dollar = (Dollar) o;
    return getAmount() == dollar.getAmount();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAmount());
  }
}

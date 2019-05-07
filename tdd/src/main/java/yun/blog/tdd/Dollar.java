package yun.blog.tdd;

import lombok.Getter;

@Getter
public class Dollar {

  private int amount;


  public Dollar(int amount) {
    this.amount = amount;
  }

  public void times(int multiplier) {

    amount = amount * multiplier;
  }
}

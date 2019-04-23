package blog.yun.transaction.book;

import blog.yun.transaction.ReadUncommitted;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookReadUncommittedApi {

  private final ReadUncommitted readUncommitted;

  @GetMapping("increase-ea")
  public void increaseEa() {
    readUncommitted.updateEa();

  }


}

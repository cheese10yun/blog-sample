package blog.yun.transaction;

import static org.assertj.core.api.Java6Assertions.assertThat;

import blog.yun.transaction.book.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ReadUncommittedTest {

  @Autowired
  private ReadUncommitted readUncommitted;

  @Test
  public void name() {

    Thread threadA = new Thread(() -> {
      final Book book = readUncommitted.updateEa();

      System.out.println("Thread A : " + book.toString());

    }, "Thread A");

    Thread threadB = new Thread(() -> {
      final Book book = readUncommitted.checkEa();

      System.out.println("Thread B : " + book.toString());

    }, "Thread B");


    threadA.start();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
//      e.printStackTrace();
    }

    threadB.start();


  }
}
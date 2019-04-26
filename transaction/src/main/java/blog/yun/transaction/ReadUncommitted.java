package blog.yun.transaction;

import blog.yun.transaction.book.Book;
import blog.yun.transaction.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReadUncommitted {

  private final BookRepository bookRepository;

  /*
   * PK 1번 : EA 1
   * Duty READ 케이스,  READ_UNCOMMITTED
   * 1. Thread A 1번책 EA  +1 증가 (Sleep)
   * 2. Thread B 1번책 EA 조회  (2로 조회)
   * 3. Thread A 1번책 EA  +1 증가 wake up 이후 exception;
   * 4 Thread B 1번책 EA 조회  (2로 조회) ???? 이것이 Duty 읽기
   */

  @Transactional(isolation = Isolation.READ_UNCOMMITTED)
  public long checkEa() {

    final String threadName = Thread.currentThread().getName();

    System.out.println(threadName + " asd");
    final Book book = bookRepository.findById(1L).orElseThrow(RuntimeException::new);

    System.out.println("ea :" + book.getEa());

    sleep(threadName);

    return book.getEa();
  }

  @Transactional
  public void updateEa() {
    final String threadName = Thread.currentThread().getName();

//    System.out.println(threadName + " ea 증가 이전");

    bookRepository.updateEa(2, 1);

//    System.out.println(threadName + " ea 증가 이후");
    sleep(threadName);

    throw new RuntimeException();

  }


  @Transactional
  public void test() {

    Thread threadA = new Thread(() -> {

      updateEa();
      System.out.println("==============");
      System.out.println("Thread A :");
      System.out.println("==============");


    }, "Thread A");

    Thread threadB = new Thread(() -> {
      final long ea = checkEa();

      System.out.println("==============");
      System.out.println("Thread B : " + ea);
      System.out.println("==============");

    }, "Thread B");

    threadA.start();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    threadB.start();
  }

  private void sleep(String threadName) {
    System.out.println(threadName + " - Sleeping");

    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
    }

    System.out.println(threadName + " - Wake up");
  }

}

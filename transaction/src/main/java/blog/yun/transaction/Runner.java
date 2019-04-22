package blog.yun.transaction;

import blog.yun.transaction.book.Book;
import blog.yun.transaction.book.BookRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Runner implements ApplicationRunner {

  private final BookRepository bookRepository;

  @Override
  public void run(ApplicationArguments args) {

    final List<Book> books = bookRepository.findAll();

    for (Book book : books) {
      System.out.println("===========");
      System.out.println(book.toString());
      System.out.println("===========");
    }

  }
}

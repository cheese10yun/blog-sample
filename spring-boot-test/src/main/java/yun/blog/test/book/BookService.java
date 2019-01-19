package yun.blog.test.book;

import org.springframework.stereotype.Service;

@Service
public class BookService {

    public Book getBook() {
        return new Book("title", 1000D);
    }

}

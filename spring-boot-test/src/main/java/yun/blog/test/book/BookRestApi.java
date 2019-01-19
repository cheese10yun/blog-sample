package yun.blog.test.book;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookRestApi {

    private final BookRestService bookRestService;

    public BookRestApi(BookRestService bookRestService) {
        this.bookRestService = bookRestService;
    }


    @GetMapping(value = "/rest/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public Book getBook() {
        return bookRestService.getRestBook();
    }



}

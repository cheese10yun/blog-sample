package com.example.testcode.di;


import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Test;

public class ContainerServiceTest {


    @Test
    public void BookRepositoryTest() {
        final BookRepository repository = ContainerService.getObject(BookRepository.class);
        then(repository).isNotNull();
    }


    @Test
    public void BookServiceTest() {
        final BookService bookService = ContainerService.getObject(BookService.class);
        then(bookService).isNotNull();
        then(bookService.bookRepository).isNotNull();
    }
}

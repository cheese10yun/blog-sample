package yun.blog.test.book;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RunWith(SpringRunner.class)
@RestClientTest(BookRestService.class)
public class BookRestServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private BookRestService bookRestService;

    @Autowired
    private MockRestServiceServer server;

    @Test
    public void rest_test() {

        server.expect(requestTo("/rest/test"))
                .andRespond(
                        withSuccess(new ClassPathResource("/test.json", getClass()), MediaType.APPLICATION_JSON)
                );

        Book book = bookRestService.getRestBook();

        assertThat(book.getId(), is(notNullValue()));
        assertThat(book.getTitle(), is("title"));
        assertThat(book.getPrice(), is(1000D));

    }
}

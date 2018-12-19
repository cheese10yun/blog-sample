package blog.yun.encapsulation.product;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderApiTest {


    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MockMvc mvc;

    @Test
    public void 정상요청() throws Exception {
        final String json = readJson("valid-request.json");
        mvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 요청바디가_유효하지않을경우() throws Exception {
        final String json = readJson("invalid-request.json");
        mvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }



    private String readJson(final String path) throws IOException {
        final InputStream inputStream = resourceLoader.getResource("classpath:" + path).getInputStream();
        final ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() {
                return inputStream;
            }
        };
        return byteSource.asCharSource(Charsets.UTF_8).read();
    }

}
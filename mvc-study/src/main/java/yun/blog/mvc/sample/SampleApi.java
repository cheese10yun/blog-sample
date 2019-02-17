package yun.blog.mvc.sample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class SampleApi {

    /**
     * preHandle -> 요청 전처리
     * postHandler -> 요청 후처리
     * afterCompletion ->
     */

    @GetMapping("/{name}")
    public Person sample(@PathVariable("name") Person person) {
        return person;
    }
}

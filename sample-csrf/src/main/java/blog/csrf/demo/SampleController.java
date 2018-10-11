package blog.csrf.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @GetMapping("/")
    public void sampleGet(){
    }

    @PostMapping("/")
    public void samplePost(){
    }

}

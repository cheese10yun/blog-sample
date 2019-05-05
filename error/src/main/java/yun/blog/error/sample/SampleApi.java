package yun.blog.error.sample;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class SampleApi {

  @GetMapping
  public void get() {

    throw new RuntimeException();

  }

  @PostMapping
  public BodySample post(@RequestBody @Valid BodySample bodySample) {
    return bodySample;
  }


  @Getter
  public static class BodySample {

    @Email
    private String email;

    @NotEmpty
    private String name;





  }


}

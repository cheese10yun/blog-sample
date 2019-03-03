package yun.blog.rabbitmqsample.member;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberApi {

  private final MemberSignUpService memberSignUpService;

  @PostMapping
  public Member doSignUp(@RequestBody SignUpRequest dto){
    return memberSignUpService.doSignUp(dto);
  }
}

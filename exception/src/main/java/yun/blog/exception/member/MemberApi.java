package yun.blog.exception.member;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberApi {

  private final MemberService memberService;

  @GetMapping("/{id}")
  public Member get(@PathVariable long id) {
    return memberService.findById(id);
  }

  @PostMapping("/unchekced")
  public Member unchekced() {
    final Member member = memberService.createUncheckedException();
    return member;

  }


  @PostMapping("/chekced")
  public Member chekced() throws IOException {
    final Member member = memberService.createCheckedException();
    return member;

  }


}

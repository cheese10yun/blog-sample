package yun.custom.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import yun.custom.validation.member.Member;
import yun.custom.validation.member.MemberRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppRunner implements ApplicationRunner {

  private final MemberRepository memberRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    final Member member = memberRepository.save(Member.builder()
        .email("yun@test.com")
        .build());


    log.info(member.toString());


  }
}

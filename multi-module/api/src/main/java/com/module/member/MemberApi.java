package com.module.member;

import com.module.core.member.Member;
import com.module.core.member.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberApi {


  private final MemberRepository memberRepository;

  @PostMapping
  public Member create() {
    final Member member = new Member("test");
    return memberRepository.save(member);
  }

  @GetMapping
  public List<Member> getMembers() {
    return memberRepository.findAll();
  }

}

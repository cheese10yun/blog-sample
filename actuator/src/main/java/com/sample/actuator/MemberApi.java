package com.sample.actuator;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberApi {

  private final MemberRepository memberRepository;

  @GetMapping
  public List<Member> getMembers() {
    final List<Member> members = new ArrayList<>();
    members.add(new Member("yun"));
    return members;

  }


}



package com.gradle.sample.member;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberApi {

  @GetMapping("/{id}")
  public Member getMember(@PathVariable long id) {

    return Member.builder()
        .id(id)
        .email("yun@test.com")
        .name("name")
        .build();

  }

}

package com.example.testcode.member;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MemberSingUpRequest {

  @NotEmpty
  private String name;

  @Email
  private String email;

  MemberSingUpRequest(final String name, final String email) {
    this.name = name;
    this.email = email;
  }
}

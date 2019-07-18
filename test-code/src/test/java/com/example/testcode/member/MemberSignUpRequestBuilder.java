package com.example.testcode.member;

public class MemberSignUpRequestBuilder {

  public static MemberSingUpRequest build(String name, String email) {
    return new MemberSingUpRequest(name, email);
  }

}

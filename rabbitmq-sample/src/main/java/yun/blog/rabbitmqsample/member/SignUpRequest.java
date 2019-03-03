package yun.blog.rabbitmqsample.member;


import lombok.Getter;

@Getter
public class SignUpRequest {

  private String name;
  private String email;

  public Member toEnttiy() {
    return Member.builder()
        .email(email)
        .name(name)
        .build();

  }

}

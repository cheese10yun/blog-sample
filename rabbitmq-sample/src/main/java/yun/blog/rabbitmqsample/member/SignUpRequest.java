package yun.blog.rabbitmqsample.member;


import lombok.Getter;

@Getter
public class SignUpRequest {

  private String name;
  private String email;

  public Member toEntity() {
    return Member.builder()
        .email(email)
        .name(name)
        .build();

  }

}

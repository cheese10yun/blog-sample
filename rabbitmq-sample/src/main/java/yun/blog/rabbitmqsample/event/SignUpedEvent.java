package yun.blog.rabbitmqsample.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import yun.blog.rabbitmqsample.member.Member;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignUpedEvent {

  private long id;
  private String email;
  private String name;

  private SignUpedEvent(long id, String email, String name) {
    this.id = id;
    this.email = email;
    this.name = name;
  }

  public static SignUpedEvent of(final Member member) {
    return new SignUpedEvent(
        member.getId(),
        member.getEmail(),
        member.getName()
    );
  }
}

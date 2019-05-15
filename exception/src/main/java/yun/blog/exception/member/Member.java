package yun.blog.exception.member;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
@Getter
public class Member {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;


  @Column(name = "name", nullable = false)
  private String name;

  public Member(String name) {
    this.name = name;
  }
}

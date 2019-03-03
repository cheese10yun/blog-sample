package yun.blog.rabbitmqsample.member;


import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false)
  private String email;

  @CreationTimestamp
  @Column(name = "create_at", nullable = false, updatable = false)
  private LocalDateTime createAt;

  @UpdateTimestamp
  @Column(name = "update_at", nullable = false)
  private LocalDateTime updateAt;

  @Builder
  public Member(String name, String email) {
    this.name = name;
    this.email = email;
  }
}

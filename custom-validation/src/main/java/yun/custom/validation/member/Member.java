package yun.custom.validation.member;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "member")
@Getter
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Email
    @Column(name = "email", nullable = false, updatable = false, unique = true)
    private String email;

    @Builder
    public Member(String email) {
        this.email = email;
    }
}

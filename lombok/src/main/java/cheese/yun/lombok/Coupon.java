package cheese.yun.lombok;

import lombok.Data;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "coupon")
@Data
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Member member;

    public Coupon(Member member) {
        this.member = member;
    }
}

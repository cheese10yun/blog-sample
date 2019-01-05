package blog.yun.encapsulation.coupon;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "amount", nullable = false, updatable = false)
    private double amount;

    @Column(name = "expiration_date", nullable = false, updatable = false)
    private LocalDate expirationDate;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;


    @Builder
    public Coupon(double amount, LocalDate expirationDate) {
        this.amount = amount;
        this.expirationDate = expirationDate;
        this.used = false;
    }

    public boolean isExpiration() {
        return LocalDate.now().isAfter(expirationDate);
    }

    public void apply() {
        verifyCouponIsAvailable();
        this.used = true;
    }

    private void verifyCouponIsAvailable() {
        verifyExpiration();
        verifyUsed();
    }

    private void verifyUsed() {
        if (used) {
            throw new IllegalStateException("이미 사용한 쿠폰입니다.");
        }
    }

    private void verifyExpiration() {
        if (LocalDate.now().isAfter(getExpirationDate())) {
            throw new IllegalStateException("사용 기간이 만료된 쿠폰입니다.");
        }
    }
}
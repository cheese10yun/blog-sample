package blog.yun.encapsulation.coupon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class FirstOrderCoupon implements CouponIssueAble {

    private final CouponRepository couponRepository;

    public FirstOrderCoupon(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public boolean canIssued() {
        // TODO: 첫 구매인지 확인 하는 로직 ...
        return true;
    }

    /**
     * 안티 패턴
     *
     * 꼬치꼬치 캐묻고 있습니다.
     */
    public void antiApply(final long couponId) {
        final Coupon coupon = couponRepository.findById(couponId).get();

        if (LocalDate.now().isAfter(coupon.getExpirationDate())) {
            throw new IllegalStateException("사용 기간이 만료된 쿠폰입니다.");
        }

        if (coupon.isUsed()) {
            throw new IllegalStateException("이미 사용한 쿠폰입니다.");
        }

        if (canIssued()) {
//            setter를 를 제공하고 있지 않아서 주석 처리
//            coupon.setUsed(false);
        }
    }

    /**
     * 좋은 패턴
     *
     * 묻지 말고 시켜라. 쿠폰 객체의 apply() 메서드를 통해서 묻지 말고 쿠폰을 적용하고 있습니다.
     */
    public void apply(final long couponId) {
        if (canIssued()) {
            final Coupon coupon = couponRepository.findById(couponId).get();
            coupon.apply();
        }
    }

}

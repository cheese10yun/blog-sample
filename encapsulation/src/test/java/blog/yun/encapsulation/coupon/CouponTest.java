package blog.yun.encapsulation.coupon;

import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CouponTest {

    @Test
    public void 쿠폰생성() {
        final double amount = 10D;
        final Coupon coupon = buildCoupon(amount, 10);

        assertThat(coupon.isUsed(), is(false));
        assertThat(coupon.getAmount(), is(amount));
        assertThat(coupon.isExpiration(), is(false));
    }


    @Test
    public void 쿠폰할인적용() {
        final double amount = 10D;
        final Coupon coupon = buildCoupon(amount, 10);

        coupon.apply();
        assertThat(coupon.isUsed(), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void 쿠폰할인적용시_이미사용했을경우() {
        final double amount = 10D;
        final Coupon coupon = buildCoupon(amount, 10);

        // 쿠폰생성시 쿠폰 사용 여부를 생성할 수 없어 apply() 두번 호출
        coupon.apply();
        coupon.apply();
    }

    @Test(expected = IllegalStateException.class)
    public void 쿠폰할인적용시_쿠폰기간만료했을경우() {
        final double amount = 10D;
        final Coupon coupon = buildCoupon(amount, -10);

        // 쿠폰생성시 쿠폰 사용 여부를 생성할 수 없어 apply() 두번 호출
        coupon.apply();
    }

    private Coupon buildCoupon(double amount, int daysToAdd) {
        return Coupon.builder()
                .amount(amount)
                .expirationDate(LocalDate.now().plusDays(daysToAdd))
                .build();
    }
}
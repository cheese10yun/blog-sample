package cheese.yun.lombok;

import cheese.yun.lombok.product.Product;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class MemberTest {

    @Test
    public void test_01() {

        final Member member = new Member("asd@asd.com", "name");
        final Coupon coupon = new Coupon(member);
        final List<Coupon> coupons = new ArrayList<>();
        coupons.add(coupon);
        member.setCoupons(coupons);

        System.out.println(member.toString());
    }

    @Test
    public void test_02() {
        Product product = new Product("name");
        assertThat(product.getId(), is(notNullValue()));
    }

    @Test
    public void test_03() {

        final Member build = Member.builder()
                .build();
    }
}
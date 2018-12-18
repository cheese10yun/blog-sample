package blog.yun.encapsulation;

import blog.yun.encapsulation.product.Order;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class OrderUnitTest {

    @Test
    // Order의 getMessageTypes 메서드를 사용 할 때 불편하다
    // 안좋은 캡술화
    public void anti_message_test_01() {
        final Order order = build("KAKAO,EMAIL,SMS");
        final String[] split = order.getMessageTypes().split(",");

        assertThat(split, hasItemInArray("KAKAO"));
        assertThat(split, hasItemInArray("EMAIL"));
        assertThat(split, hasItemInArray("SMS"));
    }

    @Test
    // KAKAO를 KAOKO 라고 잘못 입력했을 경우
    public void anti_message_test_02() {
        final Order order = build("KAOKO,EMAIL,SMS");
        final String[] split = order.getMessageTypes().split(",");

        assertThat(split, not(hasItemInArray("KAKAO")));
        assertThat(split, hasItemInArray("EMAIL"));
        assertThat(split, hasItemInArray("SMS"));
    }

    @Test
    // 메시지에 KAKAO, EMAIL, SMS 처럼 공백이 들어 간다면 실패한다
    public void anti_message_test_03() {
        final Order order = build("KAKAO, EMAIL, SMS");
        final String[] split = order.getMessageTypes().split(",");

        assertThat(split, hasItemInArray("KAKAO"));
        assertThat(split, not(hasItemInArray("EMAIL")));
        assertThat(split, not(hasItemInArray("SMS")));
    }


    @Test
    // 메시지가 없을 때 빈문자열("")을 보낼 경우
    public void anti_message_test_04() {
        final Order order = build("");
        final String[] split = order.getMessageTypes().split(",");

        assertThat(split, hasItemInArray(""));
    }


    @Test(expected = NullPointerException.class)
    // 메시지가 없을 때 null 을 보낼 경우
    public void anti_message_test_05() {
        final Order order = build(null);
        order.getMessageTypes().split(",");
    }

    @Test
    // 메시지가 중복으로 올경우
    public void anti_message_test_06() {
        final Order order = build("KAKAO, KAKAO, KAKAO");
        final String[] split = order.getMessageTypes().split(",");

        assertThat(split, hasItemInArray("KAKAO"));
        assertThat(split.length, is(3));
    }



    private Order build(String s) {
        return Order.builder()
                .messageTypes(s)
                .build();
    }
}
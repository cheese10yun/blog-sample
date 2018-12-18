package blog.yun.encapsulation;

import blog.yun.encapsulation.product.Message;
import blog.yun.encapsulation.product.MessageType;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;


public class MessageTest {

    @Test
    public void 메시지_타입이_EMAIL_KAKAO_SMS_일경우() {
        final Set<MessageType> types = new HashSet<>();
        types.add(MessageType.EMAIL);
        types.add(MessageType.KAKAO);
        types.add(MessageType.SMS);

        final Message message = Message.of(types);

        assertThat(message.getTypes(), hasItem(MessageType.EMAIL));
        assertThat(message.getTypes(), hasItem(MessageType.KAKAO));
        assertThat(message.getTypes(), hasItem(MessageType.KAKAO));
        assertThat(message.getTypes(), hasItem(MessageType.SMS));
        assertThat(message.getTypes(), hasSize(3));
    }

    @Test
    public void 메시지_타입이_EMAIL_KAKAO일경우() {
        final Set<MessageType> types = new HashSet<>();
        types.add(MessageType.EMAIL);
        types.add(MessageType.KAKAO);

        final Message message = Message.of(types);

        assertThat(message.getTypes(), hasItem(MessageType.EMAIL));
        assertThat(message.getTypes(), hasItem(MessageType.KAKAO));
        assertThat(message.getTypes(), not(hasItem(MessageType.SMS)));
        assertThat(message.getTypes(), hasSize(2));
    }

    @Test
    public void 메시지_타입이_없을경우() {
        final Set<MessageType> types = Collections.emptySet();
        final Message message = Message.of(types);

        assertThat(message.getTypes(), hasSize(0));
    }

    @Test
    public void 메시지_타입이_중복되는경우() {
        final Set<MessageType> types = new HashSet<>();
        types.add(MessageType.EMAIL);
        types.add(MessageType.EMAIL);
        types.add(MessageType.EMAIL);

        final Message message = Message.of(types);

        assertThat(message.getTypes(), hasItem(MessageType.EMAIL));
        assertThat(message.getTypes(), not(hasItem(MessageType.SMS)));
        assertThat(message.getTypes(), not(hasItem(MessageType.KAKAO)));
        assertThat(message.getTypes(), hasSize(1));
    }



}
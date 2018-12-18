package blog.yun.encapsulation.product;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Column(name = "message_type")
    private String type;

    private Message(String type) {
        this.type = StringUtils.isEmpty(type) ? null : type;
    }

    public static Message of(Set<MessageType> types) {
        return new Message(joining(types));
    }

    public List<MessageType> getTypes() {
        if (StringUtils.isEmpty(type)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(doSplit());
    }

    private static String joining(Set<MessageType> types) {
        return types.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }


    private Set<MessageType> doSplit() {
        final String[] split = this.type.split(",");
        return Arrays.stream(split)
                .map(MessageType::valueOf)
                .collect(Collectors.toSet());
    }
}

package blog.yun.encapsulation.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private Message message;

    @Column(name = "anti_message_types")
    private String messageTypes;

    @Builder
    public Order(Message message, String messageTypes) {
        this.message = message;
        this.messageTypes = messageTypes;
    }
}

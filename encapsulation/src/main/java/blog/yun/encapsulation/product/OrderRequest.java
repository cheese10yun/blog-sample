package blog.yun.encapsulation.product;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
public class OrderRequest {

    @NotNull
    private Set<MessageType> messageType;
}

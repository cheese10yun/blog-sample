package yun.custom.validation.order;


import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderApi {

    @PostMapping
    public OrderSheetRequest order(@RequestBody @Valid final OrderSheetRequest dto) {
        return dto;
    }
}

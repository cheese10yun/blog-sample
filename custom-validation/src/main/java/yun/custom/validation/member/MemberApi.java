package yun.custom.validation.member;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberApi {

    @PostMapping
    public Member create(@RequestBody @Valid final SignUpRequest dto) {


        return Member.builder()
                .email(dto.getEmail())
                .build();
    }

}

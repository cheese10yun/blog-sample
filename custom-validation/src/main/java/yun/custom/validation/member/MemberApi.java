package yun.custom.validation.member;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberApi {

    private final MemberRepository memberRepository;

    @PostMapping
    public Member create(@RequestBody @Valid final SignUpRequest dto) {
        return memberRepository.save(Member.builder()
            .email(dto.getEmail())
            .build());
    }

    @PostMapping("/custom")
    public Member createTest(@RequestBody @Valid final SignUpFormRequest dto) {
        return Member.builder()
            .email(dto.getEmail())
            .build();
    }

}

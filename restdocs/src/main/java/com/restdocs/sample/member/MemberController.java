package com.restdocs.sample.member;

import com.restdocs.sample.member.dto.MemberResponseDto;
import com.restdocs.sample.member.dto.SignUpDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("members")
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public MemberResponseDto signUp(@RequestBody @Valid SignUpDto dto) {
        return new MemberResponseDto(memberService.signUp(dto));
    }

}

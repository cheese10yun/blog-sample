package com.restdocs.sample.member;

import com.restdocs.sample.member.dto.MemberResponseDto;
import com.restdocs.sample.member.dto.SignUpDto;
import com.restdocs.sample.model.Address;
import com.restdocs.sample.model.Email;
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

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public MemberResponseDto getUser(@PathVariable Long id) {

        final Address address = Address.builder()
                .city("city")
                .street("street")
                .zipCode("code")
                .build();

        Email email = Email.builder()
                .value("test@test.com")
                .build();

        final Member member = Member.builder()
                .address(address)
                .email(email)
                .build();

        return MemberResponseDto.builder()
                .member(member)
                .build();

    }

    @GetMapping
    public MemberResponseDto getUsers(@RequestParam int page) {

        final Address address = Address.builder()
                .city("city")
                .street("street")
                .zipCode("code")
                .build();

        Email email = Email.builder()
                .value("test@test.com")
                .build();

        final Member member = Member.builder()
                .address(address)
                .email(email)
                .build();

        return MemberResponseDto.builder()
                .member(member)
                .build();

    }

}

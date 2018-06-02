package com.yun.service.member;

import com.yun.service.member.dto.PasswordDto;
import com.yun.service.member.embedded.MemberId;
import com.yun.service.member.service.ChangePasswordService;
import com.yun.service.member.service.MemberFindService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("members")
@AllArgsConstructor
public class MemberController {

    final MemberFactory memberFactory;
    final MemberFindService memberFindService;


    @RequestMapping(value = "/{id}/password", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    public void changePassword(@PathVariable("id") MemberId id, @RequestBody PasswordDto.ChangeRequest dto) {
        final ChangePasswordService changePasswordInstance = memberFactory.getChangePasswordInstance(dto);
        changePasswordInstance.change(id, dto);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public Member changePassword(@PathVariable("id") MemberId id) {
        return memberFindService.findById(id);
    }

}

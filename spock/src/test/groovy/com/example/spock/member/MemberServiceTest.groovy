package com.example.spock.member

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import spock.lang.Specification

import static org.mockito.BDDMockito.given

@SpringBootTest
class MemberServiceTest extends Specification {

    @Autowired
    private MemberService memberService;


    @MockBean
    private MemberRepository memberRepository;

    def "member create"() {

        given:
        final Member member = new Member("yun");

        given(memberRepository.save(member)).willReturn(member);

        when:
        final Member createMember = memberService.createMember("yun");

        then:
        createMember.getName() == "yun"
    }
}

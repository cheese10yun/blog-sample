package com.yun.service.member;

import com.yun.service.member.embedded.Email;
import com.yun.service.member.embedded.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, MemberId> {

    Member findByEmail(Email email);

}

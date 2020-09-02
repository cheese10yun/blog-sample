package com.example.mariadb;

import java.sql.SQLException;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MariaDbApplicationTests {

//    public MariaDB4jRule dbRule = new MariaDB4jRule(3307);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void contextLoads() throws SQLException {

//        final Member member = memberRepository.save(new Member( "name-1"));
        final Member member1 = memberRepository.save(new Member("name-2"));

//        entityManager.clear();

        final List<Member> members = memberRepository.findAll();

        System.out.println(members.get(0));
//        System.out.println(members.get(1));

    }

}

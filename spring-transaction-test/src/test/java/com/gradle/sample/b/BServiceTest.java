package com.gradle.sample.b;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class BServiceTest {



    @Test
    public void name() {

        Member member = new Member();
        member.setName("123");
        final Member asd = asd(member);

        System.out.println(member.getName());

        System.out.println(asd.getName());

    }

    public Member asd(Member member){


        member.setName("333");
        return member;

    }



    static class Member {
        private String name = "123";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
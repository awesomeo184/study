package com.study.springcore.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springcore.AppConfig;
import com.study.springcore.domain.Grade;
import com.study.springcore.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class MemberServiceTest {

    MemberService memberService;

    @BeforeEach
    void beforeEach() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        memberService = ac.getBean("memberService", MemberService.class);
    }

    @Test
    void testJoin() {
        //given
        Member memberA = new Member(1L, "memberA", Grade.BASIC);

        //when
        memberService.join(memberA);
        Member findMember = memberService.findMember(1L);

        //then
        assertThat(memberA).isEqualTo(findMember);
    }
}

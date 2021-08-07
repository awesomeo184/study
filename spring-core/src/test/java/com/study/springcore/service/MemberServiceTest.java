package com.study.springcore.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springcore.AppConfig;
import com.study.springcore.domain.Grade;
import com.study.springcore.domain.Member;
import com.study.springcore.service.MemberService;
import com.study.springcore.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberServiceTest {

    MemberService memberService;

    @BeforeEach
    void beforeEach() {
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
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

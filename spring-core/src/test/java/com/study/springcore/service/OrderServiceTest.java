package com.study.springcore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.study.springcore.domain.Grade;
import com.study.springcore.domain.Member;
import com.study.springcore.domain.Order;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    MemberService memberService = new MemberServiceImpl();
    OrderService orderService = new OrderServiceImpl();

    @Test
    void testCreateOrder() {
        //given
        Long memberId = 1L;
        Member member = new Member(memberId, "member", Grade.VIP);
        memberService.join(member);

        //when
        Order order = orderService.createOrder(memberId, "itemA", 10000);

        //then
        assertThat(order.getDiscountPrice()).isEqualTo(1000);
      
    }

}
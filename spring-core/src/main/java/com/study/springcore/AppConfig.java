package com.study.springcore;

import com.study.springcore.domain.MemberRepository;
import com.study.springcore.domain.MemoryMemberRepository;
import com.study.springcore.service.DiscountPolicy;
import com.study.springcore.service.FixDiscountPolicy;
import com.study.springcore.service.MemberService;
import com.study.springcore.service.MemberServiceImpl;
import com.study.springcore.service.OrderService;
import com.study.springcore.service.OrderServiceImpl;

public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    public OrderService orderService() {
        return new OrderServiceImpl(discountPolicy(), memberRepository());
    }

    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}

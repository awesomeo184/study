package com.study.springcore;

import com.study.springcore.domain.MemberRepository;
import com.study.springcore.domain.MemoryMemberRepository;
import com.study.springcore.service.DiscountPolicy;
import com.study.springcore.service.FixDiscountPolicy;
import com.study.springcore.service.MemberService;
import com.study.springcore.service.MemberServiceImpl;
import com.study.springcore.service.OrderService;
import com.study.springcore.service.OrderServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(discountPolicy(), memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}

package com.study.springcore.service;

import com.study.springcore.domain.Member;
import com.study.springcore.domain.MemberRepository;
import com.study.springcore.domain.MemoryMemberRepository;
import com.study.springcore.domain.Order;

public class OrderServiceImpl implements OrderService{

    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final MemberRepository memberRepository = new MemoryMemberRepository();

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);
        return new Order(memberId, itemName, itemPrice, discountPrice);
    }

}

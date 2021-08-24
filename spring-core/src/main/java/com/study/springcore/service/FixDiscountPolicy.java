package com.study.springcore.service;

import com.study.springcore.domain.Grade;
import com.study.springcore.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class FixDiscountPolicy implements DiscountPolicy{

    private static final int DISCOUNT_AMOUNT = 1000;

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return DISCOUNT_AMOUNT;
        } else {
            return 0;
        }
    }
}

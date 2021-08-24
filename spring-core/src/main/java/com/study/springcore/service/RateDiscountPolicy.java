package com.study.springcore.service;

import com.study.springcore.domain.Grade;
import com.study.springcore.domain.Member;
import org.springframework.stereotype.Component;

public class RateDiscountPolicy implements DiscountPolicy{

    private static final int DISCOUNT_PERCENTAGE = 10;

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return price * DISCOUNT_PERCENTAGE / 100;
        } else {
            return 0;
        }
    }
}

package com.study.springcore.service;

import com.study.springcore.domain.Member;

public interface DiscountPolicy {

    int discount(Member member, int price);

}

package com.study.springcore.service;

import com.study.springcore.domain.Order;

public interface OrderService {

    Order createOrder(Long memberId, String itemName, int itemPrice);

}

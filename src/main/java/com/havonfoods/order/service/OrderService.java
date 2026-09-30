package com.havonfoods.order.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.havonfoods.order.dto.CheckoutRequest;
import com.havonfoods.order.dto.OrderResponse;
import com.havonfoods.order.dto.PaymentVerificationRequest;
import com.havonfoods.user.entity.User;

public interface OrderService {
	
	OrderResponse checkout(User user, CheckoutRequest request);
    OrderResponse verifyAndCompletePayment(User user, PaymentVerificationRequest request);
    Page<OrderResponse> getUserOrders(User user, Pageable pageable);
}

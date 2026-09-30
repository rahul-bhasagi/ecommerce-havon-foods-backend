package com.havonfoods.order.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.havonfoods.common.ApiResponse;
import com.havonfoods.order.dto.CheckoutRequest;
import com.havonfoods.order.dto.OrderResponse;
import com.havonfoods.order.dto.PaymentVerificationRequest;
import com.havonfoods.order.service.OrderService;
import com.havonfoods.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CheckoutRequest request
    ) {
        OrderResponse response = orderService.checkout(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order initiated. Ready for payment."));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<OrderResponse>> verifyPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentVerificationRequest request
    ) {
        OrderResponse response = orderService.verifyAndCompletePayment(user, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment confirmed. Order is processing."));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        Page<OrderResponse> orders = orderService.getUserOrders(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }
}

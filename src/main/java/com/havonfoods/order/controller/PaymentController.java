package com.havonfoods.order.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.havonfoods.order.dto.PaymentVerificationRequestDto;
import com.havonfoods.order.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentService paymentService;

    /**
     * Frontend submits order details right after Razorpay modal succeeds
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody PaymentVerificationRequestDto requestDto) {
        boolean isSuccess = paymentService.verifyFrontendPayment(requestDto);
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Payment verified and order confirmed"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "FAILED", "message", "Invalid payment signature"));
        }
    }

    /**
     * Razorpay Server calls this webhook asynchronously
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("X-Razorpay-Signature") String signatureHeader) {
        
        paymentService.processWebhook(rawPayload, signatureHeader);
        return ResponseEntity.ok("OK");
    }
}

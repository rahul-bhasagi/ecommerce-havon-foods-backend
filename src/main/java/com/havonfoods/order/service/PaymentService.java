package com.havonfoods.order.service;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.havonfoods.cart.service.CartService;
import com.havonfoods.catalog.service.InventoryService;
import com.havonfoods.order.dto.PaymentVerificationRequestDto;
import com.havonfoods.order.dto.RazorpayOrderResponseDto;
import com.havonfoods.order.entity.Order;
import com.havonfoods.order.entity.PaymentStatus;
import com.havonfoods.order.entity.PaymentTransaction;
import com.havonfoods.order.repository.OrderRepository;
import com.havonfoods.order.repository.PaymentTransactionRepository;
import com.havonfoods.user.entity.OrderStatus;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
	
	@Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    private final PaymentTransactionRepository paymentRepo;
    private final OrderRepository orderRepo;
    
    private final InventoryService inventoryService;
    private final CartService cartService;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    /**
     * Step 1: Create Razorpay Order before payment modal opens
     */
    @Transactional
    public RazorpayOrderResponseDto createRazorpayOrder(Order order) throws RazorpayException {
        // Razorpay expects amount in paise (1 INR = 100 paise)
        int amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + order.getId());

        com.razorpay.Order rzpOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = rzpOrder.get("id");

        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderId(order.getId())
                .razorpayOrderId(razorpayOrderId)
                .amount(order.getTotalAmount())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepo.save(transaction);

        return RazorpayOrderResponseDto.builder()
                .razorpayOrderId(razorpayOrderId)
                .amount(order.getTotalAmount())
                .currency("INR")
                .keyId(keyId)
                .internalOrderId(order.getId())
                .build();
    }

    /**
     * Step 2: Immediate client-side verification
     */
    @Transactional
    public boolean verifyFrontendPayment(PaymentVerificationRequestDto dto) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", dto.getRazorpayOrderId());
            options.put("razorpay_payment_id", dto.getRazorpayPaymentId());
            options.put("razorpay_signature", dto.getRazorpaySignature());

            // Cryptographic HMAC-SHA256 verification using Razorpay SDK
            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                updatePaymentSuccess(dto.getRazorpayOrderId(), dto.getRazorpayPaymentId(), dto.getRazorpaySignature());
                return true;
            } else {
                log.warn("Invalid Razorpay signature for order: {}", dto.getRazorpayOrderId());
                return false;
            }
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Step 3: Server-to-server webhook handler (Idempotent)
     */
    @Transactional
    public void processWebhook(String webhookPayload, String signatureHeader) {
        try {
            // Verify that the webhook originated from Razorpay
            boolean isValidWebhook = Utils.verifyWebhookSignature(webhookPayload, signatureHeader, webhookSecret);
            if (!isValidWebhook) {
                throw new SecurityException("Tampered webhook signature detected!");
            }

            JSONObject event = new JSONObject(webhookPayload);
            String eventType = event.getString("event");
            JSONObject paymentEntity = event.getJSONObject("payload")
                                            .getJSONObject("payment")
                                            .getJSONObject("entity");

            String razorpayOrderId = paymentEntity.optString("order_id");
            String razorpayPaymentId = paymentEntity.optString("id");

            if ("payment.captured".equals(eventType)) {
                updatePaymentSuccess(razorpayOrderId, razorpayPaymentId, "VERIFIED_BY_WEBHOOK");
            } else if ("payment.failed".equals(eventType)) {
                String errorDesc = paymentEntity.optString("error_description", "Payment failed");
                updatePaymentFailure(razorpayOrderId, errorDesc);
            }
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage());
            throw new RuntimeException("Webhook processing error", e);
        }
    }

    private void updatePaymentSuccess(String razorpayOrderId, String paymentId, String signature) {
        paymentRepo.findByRazorpayOrderId(razorpayOrderId).ifPresent(tx -> {
            if (tx.getStatus() == PaymentStatus.CAPTURED) {
                return; // Idempotent check: already processed
            }
            tx.setRazorpayPaymentId(paymentId);
            tx.setRazorpaySignature(signature);
            tx.setStatus(PaymentStatus.CAPTURED);
            paymentRepo.save(tx);

            // Update parent Order
            orderRepo.findById(tx.getOrderId()).ifPresent(order -> {
                order.setOrderStatus(OrderStatus.CONFIRMED);
                orderRepo.save(order);
                
            order.getItems().forEach(item -> {
                    inventoryService.deductStockFefo(item.getId(), item.getQuantity());
                });

                // 3. Clear user's cart
                cartService.clearCart(order.getUser().getId());    
            });
            
        });
    }

    private void updatePaymentFailure(String razorpayOrderId, String reason) {
        paymentRepo.findByRazorpayOrderId(razorpayOrderId).ifPresent(tx -> {
            tx.setStatus(PaymentStatus.FAILED);
            tx.setFailureReason(reason);
            paymentRepo.save(tx);

            orderRepo.findById(tx.getOrderId()).ifPresent(order -> {
                order.setOrderStatus(OrderStatus.CANCELLED);
                orderRepo.save(order);
            });
        });
    }
}

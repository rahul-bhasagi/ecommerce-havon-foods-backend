package com.havonfoods.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.havonfoods.user.entity.OrderStatus;
import com.havonfoods.user.entity.PaymentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
	
	private String orderNumber;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String razorpayOrderId;
    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private Instant createdAt;

    @Data
    @Builder
    public static class OrderItemDto {
        private String productName;
        private String sku;
        private Integer weightInGrams;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private String batchNumber;
    }
}

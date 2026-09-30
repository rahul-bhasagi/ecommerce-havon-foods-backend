package com.havonfoods.order.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.havonfoods.cart.entity.Cart;
import com.havonfoods.cart.entity.CartItem;
import com.havonfoods.cart.repository.CartRepository;
import com.havonfoods.catalog.entity.BatchInventory;
import com.havonfoods.catalog.repository.BatchInventoryRepository;
import com.havonfoods.order.dto.CheckoutRequest;
import com.havonfoods.order.dto.OrderResponse;
import com.havonfoods.order.dto.PaymentVerificationRequest;
import com.havonfoods.order.entity.Order;
import com.havonfoods.order.entity.OrderItem;
import com.havonfoods.order.repository.OrderRepository;
import com.havonfoods.order.service.OrderService;
import com.havonfoods.user.entity.Address;
import com.havonfoods.user.entity.OrderStatus;
import com.havonfoods.user.entity.PaymentStatus;
import com.havonfoods.user.entity.User;
import com.havonfoods.user.repository.AddressRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	
	private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final BatchInventoryRepository batchRepository;

    @Override
    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Active cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));

        LocalDate today = LocalDate.now();
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        String orderNumber = "HVN-" + Instant.now().toEpochMilli();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingName(address.getReceiverName())
                .shippingPhone(address.getReceiverPhone())
                .shippingAddressLine1(address.getAddressLine1())
                .shippingAddressLine2(address.getAddressLine2())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingPostalCode(address.getPostalCode())
                .build();

        // FEFO (First Expired, First Out) Inventory Allocation
        for (CartItem cartItem : cart.getItems()) {
            int neededQty = cartItem.getQuantity();
            List<BatchInventory> batches = batchRepository.findAvailableBatchesByFefo(cartItem.getVariant().getId(), today);

            int totalAvailable = batches.stream().mapToInt(BatchInventory::getQuantityAvailable).sum();
            if (totalAvailable < neededQty) {
                throw new IllegalArgumentException("Insufficient stock for SKU: " + cartItem.getVariant().getSku());
            }

            for (BatchInventory batch : batches) {
                if (neededQty <= 0) break;

                int deduct = Math.min(batch.getQuantityAvailable(), neededQty);
                batch.setQuantityAvailable(batch.getQuantityAvailable() - deduct);
                batchRepository.save(batch);

                BigDecimal lineTotal = cartItem.getVariant().getSellingPrice().multiply(BigDecimal.valueOf(deduct));
                subTotal = subTotal.add(lineTotal);

                OrderItem item = OrderItem.builder()
                        .order(order)
                        .variant(cartItem.getVariant())
                        .batch(batch)
                        .productName(cartItem.getVariant().getProduct().getName())
                        .sku(cartItem.getVariant().getSku())
                        .weightInGrams(cartItem.getVariant().getWeightInGrams())
                        .unitPrice(cartItem.getVariant().getSellingPrice())
                        .quantity(deduct)
                        .lineTotal(lineTotal)
                        .build();

                orderItems.add(item);
                neededQty -= deduct;
            }
        }

        // Standard shipping: Free if subtotal > ₹500, else ₹50 flat
        BigDecimal shippingFee = subTotal.compareTo(new BigDecimal("500.00")) >= 0 
                ? BigDecimal.ZERO 
                : new BigDecimal("50.00");

        BigDecimal totalAmount = subTotal.add(shippingFee);

        // Simulated Razorpay Order ID for Phase 1 checkout integration
        String mockRazorpayOrderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        order.setSubTotal(subTotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setRazorpayOrderId(mockRazorpayOrderId);
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);

        // Clear user cart once order record is created
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToOrderResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse verifyAndCompletePayment(User user, PaymentVerificationRequest request) {
        Order order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found for given Razorpay ID"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized order modification");
        }

        order.setRazorpayPaymentId(request.getRazorpayPaymentId());
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSING);

        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(User user, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToOrderResponse);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                .map(i -> OrderResponse.OrderItemDto.builder()
                        .productName(i.getProductName())
                        .sku(i.getSku())
                        .weightInGrams(i.getWeightInGrams())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .lineTotal(i.getLineTotal())
                        .batchNumber(i.getBatch() != null ? i.getBatch().getBatchNumber() : "")
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .razorpayOrderId(order.getRazorpayOrderId())
                .subTotal(order.getSubTotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}

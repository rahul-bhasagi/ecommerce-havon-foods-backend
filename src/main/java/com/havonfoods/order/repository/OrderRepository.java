package com.havonfoods.order.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.havonfoods.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
	Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);


}

package com.havonfoods.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.havonfoods.cart.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long>{
	Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);
}

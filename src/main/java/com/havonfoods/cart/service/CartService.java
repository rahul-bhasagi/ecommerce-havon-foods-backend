package com.havonfoods.cart.service;

import com.havonfoods.cart.dto.AddToCartRequest;
import com.havonfoods.cart.dto.CartResponse;
import com.havonfoods.cart.dto.UpdateCartItemRequest;
import com.havonfoods.user.entity.User;

public interface CartService {
	
	CartResponse getCart(User currentUser, String sessionToken);
    CartResponse addItem(User currentUser, String sessionToken, AddToCartRequest request);
    CartResponse updateItem(User currentUser, String sessionToken, Long itemId, UpdateCartItemRequest request);
    CartResponse removeItem(User currentUser, String sessionToken, Long itemId);
    CartResponse mergeCart(User user, String sessionToken);
	void clearCart(Long id);
}

package com.havonfoods.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.havonfoods.cart.dto.AddToCartRequest;
import com.havonfoods.cart.dto.CartResponse;
import com.havonfoods.cart.dto.UpdateCartItemRequest;
import com.havonfoods.cart.service.CartService;
import com.havonfoods.common.ApiResponse;
import com.havonfoods.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
	
	private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal User currentUser,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken
    ) {
        CartResponse response = cartService.getCart(currentUser, sessionToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart retrieved"));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal User currentUser,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @Valid @RequestBody AddToCartRequest request
    ) {
        CartResponse response = cartService.addItem(currentUser, sessionToken, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Item added to cart"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal User currentUser,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse response = cartService.updateItem(currentUser, sessionToken, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart item updated"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal User currentUser,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @PathVariable Long itemId
    ) {
        CartResponse response = cartService.removeItem(currentUser, sessionToken, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from cart"));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartResponse>> mergeCart(
            @AuthenticationPrincipal User currentUser,
            @RequestHeader(value = "X-Session-Token", required = true) String sessionToken
    ) {
        CartResponse response = cartService.mergeCart(currentUser, sessionToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart merged successfully"));
    }
}

package com.havonfoods.cart.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.havonfoods.cart.dto.AddToCartRequest;
import com.havonfoods.cart.dto.CartItemResponse;
import com.havonfoods.cart.dto.CartResponse;
import com.havonfoods.cart.dto.UpdateCartItemRequest;
import com.havonfoods.cart.entity.Cart;
import com.havonfoods.cart.entity.CartItem;
import com.havonfoods.cart.repository.CartItemRepository;
import com.havonfoods.cart.repository.CartRepository;
import com.havonfoods.cart.service.CartService;
import com.havonfoods.catalog.entity.BatchInventory;
import com.havonfoods.catalog.entity.ProductVariant;
import com.havonfoods.catalog.repository.ProductVariantRepository;
import com.havonfoods.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    @Transactional
    public CartResponse getCart(User currentUser, String sessionToken) {
        Cart cart = getOrCreateCart(currentUser, sessionToken);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(User currentUser, String sessionToken, AddToCartRequest request) {
        Cart cart = getOrCreateCart(currentUser, sessionToken);

        ProductVariant variant = variantRepository.findByIdWithBatches(request.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Product variant not found"));

        if (!variant.isActive()) {
            throw new IllegalArgumentException("Product variant is inactive");
        }

        int availableStock = calculateAvailableStock(variant);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        int targetQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        if (targetQuantity > availableStock) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock (" + availableStock + " units available)");
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(targetQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return mapToCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateItem(User currentUser, String sessionToken, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(currentUser, sessionToken);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

        if (request.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            int availableStock = calculateAvailableStock(item.getVariant());
            if (request.getQuantity() > availableStock) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock (" + availableStock + " units available)");
            }
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        return mapToCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItem(User currentUser, String sessionToken, Long itemId) {
        Cart cart = getOrCreateCart(currentUser, sessionToken);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return mapToCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse mergeCart(User user, String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            return getCart(user, null);
        }

        Optional<Cart> guestCartOpt = cartRepository.findBySessionTokenWithItems(sessionToken);
        if (guestCartOpt.isEmpty()) {
            return getCart(user, null);
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> Cart.builder().user(user).items(new ArrayList<>()).build());

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> userItemOpt = userCart.getItems().stream()
                    .filter(ui -> ui.getVariant().getId().equals(guestItem.getVariant().getId()))
                    .findFirst();

            int availableStock = calculateAvailableStock(guestItem.getVariant());

            if (userItemOpt.isPresent()) {
                CartItem userItem = userItemOpt.get();
                int combinedQty = Math.min(userItem.getQuantity() + guestItem.getQuantity(), availableStock);
                userItem.setQuantity(combinedQty);
            } else {
                int quantity = Math.min(guestItem.getQuantity(), availableStock);
                if (quantity > 0) {
                    CartItem newItem = CartItem.builder()
                            .cart(userCart)
                            .variant(guestItem.getVariant())
                            .quantity(quantity)
                            .build();
                    userCart.getItems().add(newItem);
                }
            }
        }

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);

        return mapToCartResponse(userCart);
    }

    private Cart getOrCreateCart(User currentUser, String sessionToken) {
        if (currentUser != null) {
            return cartRepository.findByUserIdWithItems(currentUser.getId())
                    .orElseGet(() -> cartRepository.save(Cart.builder().user(currentUser).items(new ArrayList<>()).build()));
        }

        if (sessionToken != null && !sessionToken.isBlank()) {
            return cartRepository.findBySessionTokenWithItems(sessionToken)
                    .orElseGet(() -> cartRepository.save(Cart.builder().sessionToken(sessionToken).items(new ArrayList<>()).build()));
        }

        String newToken = UUID.randomUUID().toString();
        return cartRepository.save(Cart.builder().sessionToken(newToken).items(new ArrayList<>()).build());
    }

    private int calculateAvailableStock(ProductVariant variant) {
        LocalDate today = LocalDate.now();
        return variant.getBatches().stream()
                .filter(b -> b.getExpiryDate().isAfter(today))
                .mapToInt(BatchInventory::getQuantityAvailable)
                .sum();
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cart.getItems()) {
            ProductVariant v = item.getVariant();
            int stock = calculateAvailableStock(v);
            BigDecimal lineTotal = v.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            subTotal = subTotal.add(lineTotal);
            totalItems += item.getQuantity();

            itemResponses.add(CartItemResponse.builder()
                    .itemId(item.getId())
                    .variantId(v.getId())
                    .productName(v.getProduct() != null ? v.getProduct().getName() : "")
                    .sku(v.getSku())
                    .weightInGrams(v.getWeightInGrams())
                    .packagingType(v.getPackagingType())
                    .unitPrice(v.getSellingPrice())
                    .quantity(item.getQuantity())
                    .lineTotal(lineTotal)
                    .available(stock >= item.getQuantity())
                    .availableStock(stock)
                    .build());
        }

        return CartResponse.builder()
                .cartId(cart.getId())
                .sessionToken(cart.getSessionToken())
                .items(itemResponses)
                .totalItems(totalItems)
                .subTotal(subTotal)
                .build();
    }

}

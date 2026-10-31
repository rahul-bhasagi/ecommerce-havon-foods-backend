package com.havonfoods.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.havonfoods.common.ApiResponse;
import com.havonfoods.user.dto.AddressRequest;
import com.havonfoods.user.dto.AddressResponse;
import com.havonfoods.user.entity.User;
import com.havonfoods.user.service.AddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {
	
	private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(
            @AuthenticationPrincipal User user
    ) {
        List<AddressResponse> addresses = addressService.getUserAddresses(user);
        return ResponseEntity.ok(ApiResponse.success(addresses, "Addresses retrieved successfully"));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId
    ) {
        AddressResponse address = addressService.getAddressById(user, addressId);
        return ResponseEntity.ok(ApiResponse.success(address, "Address details retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse address = addressService.addAddress(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(address, "Address added successfully"));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse address = addressService.updateAddress(user, addressId, request);
        return ResponseEntity.ok(ApiResponse.success(address, "Address updated successfully"));
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId
    ) {
        AddressResponse address = addressService.setDefaultAddress(user, addressId);
        return ResponseEntity.ok(ApiResponse.success(address, "Default address set successfully"));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId
    ) {
        addressService.deleteAddress(user, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }
}

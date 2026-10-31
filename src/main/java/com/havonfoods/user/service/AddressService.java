package com.havonfoods.user.service;

import java.util.List;

import com.havonfoods.user.dto.AddressRequest;
import com.havonfoods.user.dto.AddressResponse;
import com.havonfoods.user.entity.User;

public interface AddressService {
	
	List<AddressResponse> getUserAddresses(User user);
    AddressResponse getAddressById(User user, Long addressId);
    AddressResponse addAddress(User user, AddressRequest request);
    AddressResponse updateAddress(User user, Long addressId, AddressRequest request);
    void deleteAddress(User user, Long addressId);
    AddressResponse setDefaultAddress(User user, Long addressId);
}

package com.havonfoods.user.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.havonfoods.user.dto.AddressRequest;
import com.havonfoods.user.dto.AddressResponse;
import com.havonfoods.user.entity.Address;
import com.havonfoods.user.entity.User;
import com.havonfoods.user.repository.AddressRepository;
import com.havonfoods.user.service.AddressService;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService{
	
	private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(User user) {
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(User user, Long addressId) {
        Address address = findUserAddress(user, addressId);
        return mapToResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse addAddress(User user, AddressRequest request) {
        List<Address> existing = addressRepository.findByUserId(user.getId());

        // First address added automatically becomes default
        boolean shouldBeDefault = existing.isEmpty() || request.isDefaultAddress();

        if (shouldBeDefault && !existing.isEmpty()) {
            clearExistingDefaults(existing);
        }

        Address address = Address.builder()
                .user(user)
                .receiverName(request.getReceiverName().trim())
                .receiverPhone(request.getReceiverPhone().trim())
                .addressLine1(request.getAddressLine1().trim())
                .addressLine2(request.getAddressLine2() != null ? request.getAddressLine2().trim() : null)
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .postalCode(request.getPostalCode().trim())
                .defaultAddress(shouldBeDefault)
                .build();

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(User user, Long addressId, AddressRequest request) {
        Address address = findUserAddress(user, addressId);

        if (request.isDefaultAddress() && !address.isDefaultAddress()) {
            List<Address> existing = addressRepository.findByUserId(user.getId());
            clearExistingDefaults(existing);
            address.setDefaultAddress(true);
        }

        address.setReceiverName(request.getReceiverName().trim());
        address.setReceiverPhone(request.getReceiverPhone().trim());
        address.setAddressLine1(request.getAddressLine1().trim());
        address.setAddressLine2(request.getAddressLine2() != null ? request.getAddressLine2().trim() : null);
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPostalCode(request.getPostalCode().trim());

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(User user, Long addressId) {
        Address address = findUserAddress(user, addressId);
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        // If default address was deleted, promote another address if one exists
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserId(user.getId());
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(User user, Long addressId) {
        Address targetAddress = findUserAddress(user, addressId);

        List<Address> existing = addressRepository.findByUserId(user.getId());
        clearExistingDefaults(existing);

        targetAddress.setDefaultAddress(true);
        return mapToResponse(addressRepository.save(targetAddress));
    }

    private Address findUserAddress(User user, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found or does not belong to user"));
    }

    private void clearExistingDefaults(List<Address> addresses) {
        for (Address addr : addresses) {
            if (addr.isDefaultAddress()) {
                addr.setDefaultAddress(false);
                addressRepository.save(addr);
            }
        }
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}

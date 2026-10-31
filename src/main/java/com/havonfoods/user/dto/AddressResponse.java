package com.havonfoods.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
	
	private Long id;
    private String receiverName;
    private String receiverPhone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private boolean defaultAddress;
}

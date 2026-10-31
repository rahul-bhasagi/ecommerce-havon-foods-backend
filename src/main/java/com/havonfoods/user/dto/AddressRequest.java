package com.havonfoods.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {
	
	@NotBlank(message = "Receiver name is required")
    @Size(min = 2, max = 100, message = "Receiver name must be between 2 and 100 characters")
    private String receiverName;

    @NotBlank(message = "Receiver phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Must be a valid 10-digit Indian phone number")
    private String receiverPhone;

    @NotBlank(message = "Address Line 1 is required")
    @Size(max = 255, message = "Address Line 1 cannot exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address Line 2 cannot exceed 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @NotBlank(message = "PIN code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Must be a valid 6-digit Indian PIN code")
    private String postalCode;

    private boolean defaultAddress;
}

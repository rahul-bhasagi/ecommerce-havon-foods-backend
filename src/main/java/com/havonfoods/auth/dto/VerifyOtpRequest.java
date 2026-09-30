package com.havonfoods.auth.dto;

import com.havonfoods.auth.entity.OtpPurpose;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {
	@NotBlank(message = "Identifier (email or phone) is required")
    private String identifier;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit number")
    private String otpCode;

    @NotNull(message = "OTP purpose is required")
    private OtpPurpose purpose;
}

package com.havonfoods.auth.service;

import com.havonfoods.auth.dto.AuthResponse;
import com.havonfoods.auth.dto.LoginRequest;
import com.havonfoods.auth.dto.RegisterRequest;
import com.havonfoods.auth.dto.VerifyOtpRequest;

public interface AuthService {
	void register(RegisterRequest request);
    AuthResponse verifyRegistrationOtp(VerifyOtpRequest request);
    AuthResponse login(LoginRequest request);
}

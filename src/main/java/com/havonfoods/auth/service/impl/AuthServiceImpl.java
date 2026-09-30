package com.havonfoods.auth.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.havonfoods.auth.dto.AuthResponse;
import com.havonfoods.auth.dto.LoginRequest;
import com.havonfoods.auth.dto.RegisterRequest;
import com.havonfoods.auth.dto.VerifyOtpRequest;
import com.havonfoods.auth.entity.OtpPurpose;
import com.havonfoods.auth.security.JwtService;
import com.havonfoods.auth.service.AuthService;
import com.havonfoods.auth.service.OtpService;
import com.havonfoods.user.entity.Role;
import com.havonfoods.user.entity.User;
import com.havonfoods.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
	
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_CUSTOMER)
                .active(false)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        userRepository.save(user);

        String targetIdentifier = request.getPhoneNumber() != null ? request.getPhoneNumber() : request.getEmail();
        otpService.sendOtp(targetIdentifier, OtpPurpose.REGISTRATION);
    }

    @Override
    @Transactional
    public AuthResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getIdentifier(), request.getOtpCode(), request.getPurpose());
        if (!isValid) {
            throw new BadCredentialsException("Invalid or expired OTP");
        }

        User user = userRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setActive(true);
        if (request.getIdentifier().contains("@")) {
            user.setEmailVerified(true);
        } else {
            user.setPhoneVerified(true);
        }
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(900L)
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        User user = userRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(900L)
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}

package com.havonfoods.auth.service.impl;


import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.havonfoods.auth.entity.Otp;
import com.havonfoods.auth.entity.OtpPurpose;
import com.havonfoods.auth.repository.OtpRepository;
import com.havonfoods.auth.service.OtpService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsoleMockOtpServiceImpl implements OtpService {

	private static final Logger log = LoggerFactory.getLogger(ConsoleMockOtpServiceImpl.class);
    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void sendOtp(String identifier, OtpPurpose purpose) {
        int code = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(code);

        Otp otp = Otp.builder()
                .identifier(identifier)
                .otpCode(otpCode)
                .purpose(purpose)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .used(false)
                .build();

        otpRepository.save(otp);

        log.info("=================================================");
        log.info(" [MOCK OTP SERVICE] Havon Foods");
        log.info(" Recipient: {}", identifier);
        log.info(" Purpose:   {}", purpose);
        log.info(" OTP Code:  {}", otpCode);
        log.info(" Valid for: 5 minutes");
        log.info("=================================================");
    }

    @Override
    @Transactional
    public boolean verifyOtp(String identifier, String code, OtpPurpose purpose) {
        return otpRepository.findTopByIdentifierAndPurposeAndUsedFalseOrderByCreatedAtDesc(identifier, purpose)
                .filter(otp -> otp.getExpiresAt().isAfter(Instant.now()))
                .filter(otp -> otp.getOtpCode().equals(code))
                .map(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }

}

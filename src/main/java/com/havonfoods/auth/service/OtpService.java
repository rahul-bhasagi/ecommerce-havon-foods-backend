package com.havonfoods.auth.service;

import com.havonfoods.auth.entity.OtpPurpose;

public interface OtpService {
	void sendOtp(String identifier, OtpPurpose purpose);
    boolean verifyOtp(String identifier, String code, OtpPurpose purpose);
}

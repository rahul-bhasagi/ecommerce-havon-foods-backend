package com.havonfoods.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.havonfoods.auth.entity.Otp;
import com.havonfoods.auth.entity.OtpPurpose;

public interface OtpRepository extends JpaRepository<Otp, Long>{
	Optional<Otp> findTopByIdentifierAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String identifier, 
            OtpPurpose purpose
    );
}

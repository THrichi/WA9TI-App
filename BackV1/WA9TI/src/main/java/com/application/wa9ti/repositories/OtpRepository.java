package com.application.wa9ti.repositories;

import com.application.wa9ti.models.OtpCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);

    @Transactional
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expirationTime < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
}

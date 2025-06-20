package com.application.wa9ti.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phoneNumber; // Numéro de téléphone unique

    @Column(nullable = false)
    private String otp; // Code OTP

    @Column(nullable = false)
    private LocalDateTime expirationTime; // Date d'expiration

    @Column(nullable = false)
    private int attemptsRemaining = 3; // Nombre de tentatives restantes

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public void decrementAttempts() {
        this.attemptsRemaining--;
    }
}

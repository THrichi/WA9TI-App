package com.application.wa9ti.services.reCaptcha;

import com.application.wa9ti.models.OtpCode;
import com.application.wa9ti.repositories.OtpRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class OtpServiceImp implements OtpService{
    private final OtpRepository otpRepository;
    private final SmsService smsService;
    /**
     * 🔹 Génère un OTP et l'envoie par SMS
     */
    @Override
    @Transactional
    public boolean sendOTP(String phoneNumber) {
        // Supprime tout ancien OTP avant d'en générer un nouveau
        otpRepository.deleteByPhoneNumber(phoneNumber);
        otpRepository.flush();
        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5); // Expiration dans 5 min

        OtpCode otpCode = new OtpCode();
        otpCode.setPhoneNumber(phoneNumber);
        otpCode.setOtp(otp);
        otpCode.setExpirationTime(expirationTime);
        otpRepository.save(otpCode);
        System.out.println(otp);
        return smsService.sendSmsViaBulkSms(phoneNumber, otp);
    }

    /**
     * 🔹 Vérifie si le code OTP entré par l'utilisateur est correct
     */
    @Override
    @Transactional
    public boolean verifyOTP(String phoneNumber, String enteredOtp) {
        OtpCode otpCode = otpRepository.findByPhoneNumber(phoneNumber).orElse(null);

        if (otpCode == null) {
            return false;
        }

        // Vérifier si l'OTP est expiré
        if (otpCode.isExpired()) {
            otpRepository.deleteByPhoneNumber(phoneNumber);
            return false;
        }

        // Vérification du code entré
        if (!otpCode.getOtp().equals(enteredOtp)) {
            otpCode.decrementAttempts();
            if (otpCode.getAttemptsRemaining() <= 0) {
                otpRepository.deleteByPhoneNumber(phoneNumber); // Supprime après trop d'échecs
            } else {
                otpRepository.save(otpCode); // Mise à jour des tentatives restantes
            }
            return false;
        }

        // Si le code est correct, on le supprime après validation
        otpRepository.deleteByPhoneNumber(phoneNumber);
        return true;
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000)); // Génère un code à 6 chiffres
    }
}

package com.application.wa9ti.controllers;

import com.application.wa9ti.services.reCaptcha.OtpService;
import com.application.wa9ti.services.reCaptcha.RecaptchaService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/public/sms")
@AllArgsConstructor
public class SmsController {
    private final RecaptchaService recaptchaService;
    private final OtpService otpService;

    // Stockage des buckets par IP
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();


    // Méthode pour limiter le nombre d'envois par IP
    private Bucket newBucket() {
        Refill perMinuteRefill = Refill.greedy(1, Duration.ofSeconds(60)); // 1 SMS par minute
        Bandwidth perMinuteLimit = Bandwidth.classic(1, perMinuteRefill);

        Refill dailyRefill = Refill.greedy(20, Duration.ofDays(1)); // 20 SMS max par jour
        Bandwidth dailyBandwidth = Bandwidth.classic(20, dailyRefill);

        return Bucket.builder()
                .addLimit(perMinuteLimit)
                .addLimit(dailyBandwidth)
                .build();
    }

    /**
     * 🔹 Envoie un OTP après vérification du reCAPTCHA et de la limite d'envoi
     */
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOTP(@RequestBody Map<String, String> requestData, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> newBucket());

        // Vérification du quota d'envoi par IP
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.badRequest().body("Trop de demandes, réessayez plus tard.");
        }

        String recaptchaToken = requestData.get("recaptchaToken");
        String phoneNumber = requestData.get("phone");

        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ResponseEntity.badRequest().body("Le numéro de téléphone est requis.");
        }

        // Vérification reCAPTCHA
        boolean isValidCaptcha = recaptchaService.isValidRecaptcha(recaptchaToken);
        if (!isValidCaptcha) {
            return ResponseEntity.badRequest().body("Échec de validation du reCAPTCHA.");
        }

        // Envoi de l'OTP via le service
        boolean success = otpService.sendOTP(phoneNumber);
        return success
                ? ResponseEntity.ok("OTP envoyé avec succès.")
                : ResponseEntity.badRequest().body("Échec de l'envoi du SMS.");
    }

    /**
     * 🔹 Vérifie si le code OTP saisi est correct
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Boolean> verifyOtp(@RequestBody Map<String, String> requestData) {
        String phoneNumber = requestData.get("phone");
        String otp = requestData.get("otp");

        if (phoneNumber == null || phoneNumber.isBlank() || otp == null || otp.isBlank()) {
            return ResponseEntity.badRequest().body(false);
        }

        boolean isValid = otpService.verifyOTP(phoneNumber, otp);
        return ResponseEntity.ok(isValid);
    }
}

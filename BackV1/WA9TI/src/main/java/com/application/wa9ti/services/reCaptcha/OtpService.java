package com.application.wa9ti.services.reCaptcha;

public interface OtpService {
    boolean sendOTP(String phoneNumber);
    boolean verifyOTP(String phoneNumber, String enteredOtp);
}

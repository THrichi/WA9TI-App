package com.application.wa9ti.services.reCaptcha;

public interface SmsService {
    boolean sendSmsViaBulkSms(String phoneNumber, String otp);
}

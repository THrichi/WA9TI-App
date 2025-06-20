package com.application.wa9ti.services.reCaptcha;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SmsServiceImp implements SmsService{
    @Override
    public boolean sendSmsViaBulkSms(String phoneNumber, String otp) {
        return true;
    }
}

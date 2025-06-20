package com.application.wa9ti.services.reCaptcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class RecaptchaService {

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    public boolean isValidRecaptcha(String token) {
        // Prépare la requête
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        // On utilise un MultiValueMap pour envoyer les paramètres en POST
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", recaptchaSecret);
        formData.add("response", token);

        // Effectue la requête
        ResponseEntity<Map> response = restTemplate.postForEntity(
                RECAPTCHA_VERIFY_URL,
                formData,
                Map.class
        );

        // Vérifie la réponse
        Map body = response.getBody();
        if (body != null && Boolean.TRUE.equals(body.get("success"))) {
            // Récupère le score (si présent)
            double score = body.get("score") instanceof Number
                    ? ((Number) body.get("score")).doubleValue()
                    : 0.0;
            return score >= 0.5;
        }

        return false;
    }
}

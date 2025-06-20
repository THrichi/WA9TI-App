package com.application.wa9ti.services.auth;

import com.application.wa9ti.enums.SubRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class InvitationTokenService {

    @Value("${jwt.invitation.secret}")
    private String secretKey;

    public static final long INVITATION_EXPIRATION_SECONDS = TimeUnit.DAYS.toSeconds(2); // 2 jours


    public String generateInvitationToken(String email, Long storeId, SubRole subRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("storeId", storeId);
        claims.put("subRole", subRole.name());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + INVITATION_EXPIRATION_SECONDS  * 1000))
                .and()
                .signWith(getKey())
                .compact();
    }

    // Valider un token d'invitation
    public boolean validateInvitationToken(String token) {
        return !isTokenExpired(token);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Vérifier si le token a expiré
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


package com.serviceabonnement.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class SystemTokenProvider {

    @Value("${jwt.secret:G10_SECRET_KEY_SGITU_2025_SUPER_SECURE_KEY_32CHARS}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    public String generateSystemToken() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject("system-scheduler")
                .claim("roles", Collections.singletonList("ROLE_ADMIN_G2"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000)) // 1 minute is enough for a batch
                .signWith(key)
                .compact();
    }
}

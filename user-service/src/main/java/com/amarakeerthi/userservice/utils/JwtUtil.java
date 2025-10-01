package com.amarakeerthi.userservice.utils;

import com.amarakeerthi.userservice.constants.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.access-token.expiry-ms}")
    private Long jwtExpirationMs;
    
    public String generateToken(Long userId, String email, UserRole role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtExpirationMs, ChronoUnit.MILLIS);
        
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role.name())
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }
    
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error extracting claims from JWT token", e);
            throw new RuntimeException("Invalid JWT token");
        }
    }
    
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
    
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }
    
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public boolean validateToken(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return (email.equals(tokenEmail) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public Long getExpirationTime() {
        return jwtExpirationMs;
    }
}
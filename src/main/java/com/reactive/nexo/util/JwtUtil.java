package com.reactive.nexo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation1234567890}")
    private String jwtSecret;

    @Value("${jwt.reset-password-expiration:3600000}")  // Default 1 hour in milliseconds
    private long resetPasswordExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate a JWT token for password reset with employee_id claim and 1 hour expiration
     */
    public String generatePasswordResetToken(String employeeEmail,String employeeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employee_email", employeeEmail);
        claims.put("employee_id", employeeId);
        claims.put("purpose", "password_reset");

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + resetPasswordExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract claims from a token
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("Failed to extract claims from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate a password reset token
     */
    public boolean validatePasswordResetToken(String token) {
        try {
            Claims claims = extractClaims(token);
            if (claims == null) return false;
            
            // Check if token is for password reset
            String purpose = (String) claims.get("purpose");
            if (!"password_reset".equals(purpose)) return false;
            
            // Check if token is not expired
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
            
        } catch (Exception e) {
            log.warn("Password reset token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract employee_id from password reset token
     */
    public Integer extractEmployeeIdFromResetToken(String token) {
        try {
            Claims claims = extractClaims(token);
            if (claims == null) return null;
            
            String purpose = (String) claims.get("purpose");
            if (!"password_reset".equals(purpose)) return null;
            
            String employeeId = (String) claims.get("employee_id");
            return employeeId != null ? Integer.parseInt(employeeId) : null;
            
        } catch (Exception e) {
            log.warn("Failed to extract employee_id from reset token: {}", e.getMessage());
            return null;
        }
    }
}
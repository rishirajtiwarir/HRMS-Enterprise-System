package com.enterprise.hrms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility component to issue, parse, and validate JSON Web Tokens (JWT).
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${hrms.jwt.secret}")
    private String jwtSecret;

    @Value("${hrms.jwt.expiration-ms}")
    private int jwtExpirationInMs;

    @Value("${hrms.jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationInMs;

    private Key getSigningKey() {
        // Fallback or decodes hex/string secret key. Needs to be at least 256 bits (32 bytes).
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            // Guarantee minimum length to prevent weak key exceptions
            return Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT access token for logged in user
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Generate token from username (useful during token refreshes)
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Retrieve username from JWT token subject
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // Validate if the JWT token is valid and unexpired
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

    public long getJwtRefreshExpirationInMs() {
        return jwtRefreshExpirationInMs;
    }
}

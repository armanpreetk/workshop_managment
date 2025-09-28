package org.example.workshop_managment.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;
    private final JwtParser parser;

    public JwtUtil(String secret, long expirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expirationMs;
    this.parser = Jwts.parser().verifyWith(key).build();
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        return parser.parseSignedClaims(token);
    }

    public String getUsername(String token) {
        return validateToken(token).getPayload().getSubject();
    }

    public Map<String, Object> getClaims(String token) {
        return validateToken(token).getPayload();
    }
}

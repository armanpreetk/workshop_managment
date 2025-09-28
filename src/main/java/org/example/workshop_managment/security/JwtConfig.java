package org.example.workshop_managment.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret:mysecretkeymysecretkeymysecretkeymysecretkey}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secret, expirationMs);
    }
}

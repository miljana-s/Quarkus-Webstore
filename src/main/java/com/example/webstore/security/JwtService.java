package com.example.webstore.security;

import io.smallrye.jwt.build.Jwt;

import java.time.Duration;
import java.util.Set;

public class JwtService {
    private static final String ISSUER = "webstore-app";

    public static String generateToken(Long userId, String username, Set<String> roles) {
        return Jwt.issuer(ISSUER)
                .upn(username)
                .subject(String.valueOf(userId))
                .groups(roles)
                .claim("userId", userId)
                .expiresIn(Duration.ofHours(12))
                .sign();
    }
}

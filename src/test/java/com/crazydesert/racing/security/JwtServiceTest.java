package com.crazydesert.racing.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    @Test
    void generatesAndReadsTokenWithConfiguredSecret() {
        JwtService jwtService = new JwtService(
                "test-only-jwt-secret-with-at-least-32-bytes"
        );

        String token = jwtService.generateToken("owner@example.com");

        assertEquals("owner@example.com", jwtService.extractEmail(token));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(
                IllegalStateException.class,
                () -> new JwtService("too-short")
        );
    }
}

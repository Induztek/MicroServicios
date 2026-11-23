package com.induztek.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // Clave HMAC-SHA256 de al menos 32 bytes (256 bits)
    private static final String SECRETO_PRUEBA = "ClaveSecretaDePruebaParaInduztekIngenieria2026!";

    @Test
    void crearToken_generaJwtValidoConClaimsEsperados() {
        JwtService jwtService = new JwtService(SECRETO_PRUEBA, 60L);

        String token = jwtService.crearToken(7L, 3L);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);

        SecretKey key = Keys.hmacShaKeyFor(SECRETO_PRUEBA.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("7", claims.getSubject());
        assertEquals(3, claims.get("rol", Integer.class));
        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }
}
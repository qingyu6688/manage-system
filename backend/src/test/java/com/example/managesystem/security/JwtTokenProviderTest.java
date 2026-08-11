package com.example.managesystem.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = "unit-test-secret-must-have-32-bytes";
    private static final String SEEDED_PASSWORD_HASH =
            "$2b$10$F55Wnt/hnJ/QHzGsSkDkZeuuOrhzSKJLgnc.9WaIMFIDPZKvV.avy";

    @Test
    void shouldCreateAndValidateAccessToken() {
        JwtTokenProvider provider = new JwtTokenProvider(TEST_SECRET, 30, 7);
        LoginUser user = new LoginUser(
                1L,
                "admin",
                "encoded-password",
                true,
                List.of(new SimpleGrantedAuthority("R_SUPER"))
        );

        String token = provider.createAccessToken(user);

        assertEquals("admin", provider.getUsername(token));
        assertTrue(provider.isAccessToken(token));
        assertFalse(provider.isRefreshToken(token));
        assertTrue(provider.isValid(token, user));
    }

    @Test
    void shouldRejectShortSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider("too-short", 30, 7));
    }

    @Test
    void seededAdminPasswordShouldMatchDocumentation() {
        assertTrue(new BCryptPasswordEncoder().matches("Admin@123", SEEDED_PASSWORD_HASH));
    }
}

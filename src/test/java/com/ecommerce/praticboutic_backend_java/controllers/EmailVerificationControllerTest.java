package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.models.JwtPayload;
import com.ecommerce.praticboutic_backend_java.requests.EmailVerificationRequest;
import com.ecommerce.praticboutic_backend_java.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ... existing code ...

class EmailVerificationControllerTest {

    private EmailVerificationController controller;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class, Answers.RETURNS_DEEP_STUBS);
        controller = new EmailVerificationController();
        inject(controller, "jdbcTemplate", jdbcTemplate);
        inject(controller, "sessionMaxLifetime", 3600L);
    }

    @Test
    @DisplayName("verifyEmail - email libre -> OK et token renvoyé")
    void verifyEmail_ok_whenEmailNotExists() {
        EmailVerificationRequest req = new EmailVerificationRequest();
        req.setEmail("new@example.com");

        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(*) FROM client c WHERE c.email = ?"),
                eq(Integer.class),
                eq("new@example.com")
        )).thenReturn(0);

        Map<String, Object> payload = new HashMap<>();
        try (MockedStatic<JwtService> jwtStatic = Mockito.mockStatic(JwtService.class)) {
            jwtStatic.when(() -> JwtService.parseToken("tok"))
                    .thenReturn(new JwtPayload(null, null, payload));
            jwtStatic.when(() -> JwtService.generateToken(anyMap(), anyString()))
                    .thenReturn("new.jwt");

            ResponseEntity<?> resp = controller.verifyEmail(req, "Bearer tok");

            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("OK", body.get("result"));
            assertEquals("new.jwt", body.get("token"));
        }
    }

    @Test
    @DisplayName("verifyEmail - email déjà pris -> KO")
    void verifyEmail_ko_whenEmailExists() {
        EmailVerificationRequest req = new EmailVerificationRequest();
        req.setEmail("used@example.com");

        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(*) FROM client c WHERE c.email = ?"),
                eq(Integer.class),
                eq("used@example.com")
        )).thenReturn(1);

        Map<String, Object> payload = new HashMap<>();
        try (MockedStatic<JwtService> jwtStatic = Mockito.mockStatic(JwtService.class)) {
            jwtStatic.when(() -> JwtService.parseToken("tok"))
                    .thenReturn(new JwtPayload(null, null, payload));
            jwtStatic.when(() -> JwtService.generateToken(anyMap(), anyString()))
                    .thenReturn("new.jwt");

            ResponseEntity<?> resp = controller.verifyEmail(req, "Bearer tok");

            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("KO", body.get("result"));
            assertEquals("new.jwt", body.get("token"));
        }
    }

    @Test
    @DisplayName("verifyEmail - exception DB -> 500")
    void verifyEmail_error_onDbException() {
        EmailVerificationRequest req = new EmailVerificationRequest();
        req.setEmail("err@example.com");

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any()))
                .thenThrow(new RuntimeException("db error"));

        Map<String, Object> payload = new HashMap<>();
        try (MockedStatic<JwtService> jwtStatic = Mockito.mockStatic(JwtService.class)) {
            jwtStatic.when(() -> JwtService.parseToken("tok"))
                    .thenReturn(new JwtPayload(null, null, payload));

            ResponseEntity<?> resp = controller.verifyEmail(req, "Bearer tok");

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
            Object body = resp.getBody();
            assertNotNull(body);
            assertTrue(body.toString().contains("error"));
        }
    }

    private static void inject(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            fail("Injection échouée: " + field + " - " + e.getMessage());
        }
    }
}
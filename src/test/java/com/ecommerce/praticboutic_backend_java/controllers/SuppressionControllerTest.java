package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.SuppressionRequest;
import com.ecommerce.praticboutic_backend_java.services.JwtService;
import com.ecommerce.praticboutic_backend_java.services.SuppressionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ... existing code ...

class SuppressionControllerTest {

    private SuppressionController controller;
    private SuppressionService suppressionService;
    private JwtService jwtService;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        controller = new SuppressionController();
        suppressionService = mock(SuppressionService.class, Answers.RETURNS_DEEP_STUBS);
        jwtService = mock(JwtService.class, Answers.RETURNS_DEEP_STUBS);
        httpRequest = mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);

        inject(controller, "suppressionService", suppressionService);
        inject(controller, "jwtService", jwtService);
    }

    @Test
    @DisplayName("supprimerCompte - 401 si non authentifié")
    void supprimerCompte_unauthenticated() {
        SuppressionRequest req = new SuppressionRequest();
        when(jwtService.isAuthenticated(anyMap())).thenReturn(false);

        ResponseEntity<?> resp = controller.supprimerCompte(req, httpRequest, "Bearer tok");

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    @DisplayName("supprimerCompte - OK appelle service et retourne 200")
    void supprimerCompte_ok() throws Exception {
        SuppressionRequest req = new SuppressionRequest();
        when(jwtService.isAuthenticated(anyMap())).thenReturn(true);
        when(httpRequest.getRemoteAddr()).thenReturn("1.2.3.4");
        doNothing().when(suppressionService).supprimerCompte(eq(req), eq("1.2.3.4"));

        ResponseEntity<?> resp = controller.supprimerCompte(req, httpRequest, "Bearer tok");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("OK", ((java.util.Map<?, ?>) resp.getBody()).get("result"));
        verify(suppressionService).supprimerCompte(eq(req), eq("1.2.3.4"));
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
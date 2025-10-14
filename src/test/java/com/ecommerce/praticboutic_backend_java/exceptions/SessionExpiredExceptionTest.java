package com.ecommerce.praticboutic_backend_java.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionExpiredExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Session expirée";
        SessionExpiredException exception = new SessionExpiredException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Session expirée, veuillez vous reconnecter";
        Throwable cause = new RuntimeException("Jeton invalide");

        SessionExpiredException exception = new SessionExpiredException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithCauseOnly() {
        Throwable cause = new IllegalStateException("Données de session absentes");
        SessionExpiredException exception = new SessionExpiredException("Test");

        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("Données de session absentes"));
    }

    @Test
    void testIsRuntimeException() {
        SessionExpiredException exception = new SessionExpiredException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}

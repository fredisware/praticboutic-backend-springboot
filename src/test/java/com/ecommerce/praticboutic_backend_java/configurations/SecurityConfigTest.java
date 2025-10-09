package com.ecommerce.praticboutic_backend_java.configurations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

// ... existing code ...

class SecurityConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(SecurityConfig.class);

    @Test
    @DisplayName("passwordEncoder - bean présent et BCrypt fonctionnel")
    void passwordEncoder_bean_present_and_works() {
        contextRunner.run(ctx -> {
            assertTrue(ctx.containsBean("passwordEncoder"));
            PasswordEncoder encoder = ctx.getBean(PasswordEncoder.class);
            assertNotNull(encoder);
            String raw = "secret";
            String hash = encoder.encode(raw);
            assertTrue(encoder.matches(raw, hash));
        });
    }

    @Test
    @DisplayName("securityFilterChain - bean présent et construit sans erreur")
    void securityFilterChain_bean_present() {
        contextRunner.run(ctx -> {
            assertTrue(ctx.containsBean("securityFilterChain"));
            SecurityFilterChain chain = ctx.getBean(SecurityFilterChain.class);
            assertNotNull(chain);
        });
    }
}
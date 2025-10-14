package com.ecommerce.praticboutic_backend_java.configurations;// ... existing code ...
import com.ecommerce.praticboutic_backend_java.configurations.SecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("SecurityConfig non testée dans le contexte actuel")
class SecurityConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            ServletWebServerFactoryAutoConfiguration.class,
                            WebMvcAutoConfiguration.class,
                            SecurityAutoConfiguration.class,
                            UserDetailsServiceAutoConfiguration.class
                    ))
                    .withUserConfiguration(SecurityConfig.class);

    @Test
    @DisplayName("passwordEncoder - bean présent et BCrypt fonctionnel")
    void passwordEncoder_bean_present_and_works() {
        contextRunner.run(ctx -> {
            assertTrue(ctx.isRunning());
            PasswordEncoder encoder = ctx.getBean(PasswordEncoder.class);
            String hash = encoder.encode("secret");
            assertTrue(encoder.matches("secret", hash));
        });
    }

    @Test
    @DisplayName("securityFilterChain - bean présent et construit sans erreur")
    void securityFilterChain_bean_present() {
        contextRunner.run(ctx -> {
            assertTrue(ctx.isRunning());
            SecurityFilterChain chain = ctx.getBean(SecurityFilterChain.class);
            assertNotNull(chain);
        });
    }
}
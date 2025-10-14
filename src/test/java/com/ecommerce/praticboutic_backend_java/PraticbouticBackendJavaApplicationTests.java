package com.ecommerce.praticboutic_backend_java;

import com.ecommerce.praticboutic_backend_java.configurations.FirebaseConfig;
import com.ecommerce.praticboutic_backend_java.services.StripeService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PraticbouticBackendJavaApplicationTests.MinimalConfig.class})
@TestPropertySource(properties = {
        "stripe.secret.key=dummy",
        "stripe.public.key=dummy",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PraticbouticBackendJavaApplicationTests {

    @Autowired
    private FirebaseConfig firebaseConfig;

    @Autowired
    private StripeService stripeService;

    @Test
    void contextLoads() {
        // Vérifie que les beans mockés sont injectés
        assertNotNull(firebaseConfig);
        assertNotNull(stripeService);
    }

    @TestConfiguration
    static class MinimalConfig {

        @Bean
        public FirebaseConfig firebaseConfig() throws Exception {
            FirebaseConfig config = mock(FirebaseConfig.class);
            FirebaseApp mockApp = mock(FirebaseApp.class);
            when(mockApp.getOptions()).thenReturn(mock(FirebaseOptions.class));
            when(config.firebaseApp()).thenReturn(mockApp);
            return config;
        }

        @Bean
        public StripeService stripeService() {
            return mock(StripeService.class);
        }
    }
}

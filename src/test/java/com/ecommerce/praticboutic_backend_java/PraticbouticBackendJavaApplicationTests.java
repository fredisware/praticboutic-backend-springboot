package com.ecommerce.praticboutic_backend_java;

import com.ecommerce.praticboutic_backend_java.configurations.FirebaseConfig;
import com.ecommerce.praticboutic_backend_java.services.StripeService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {
        "stripe.secret.key=dummy",
        "stripe.public.key=dummy",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@SpringBootTest
@ActiveProfiles("test")
class PraticbouticBackendJavaApplicationTests {

    @Autowired
    private FirebaseConfig firebaseConfig;

    @Autowired
    private StripeService stripeService;

    @Test
    void contextLoads() {
        assertNotNull(firebaseConfig, "FirebaseConfig should be loaded");
        assertNotNull(stripeService, "StripeService should be loaded");
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public com.google.firebase.messaging.FirebaseMessaging firebaseMessagingMock() {
            return mock(com.google.firebase.messaging.FirebaseMessaging.class);
        }


        @Bean
        @Primary
        public FirebaseConfig firebaseConfig() throws IOException {
            FirebaseConfig mockConfig = mock(FirebaseConfig.class);
            FirebaseApp mockApp = mock(FirebaseApp.class);
            when(mockApp.getOptions()).thenReturn(mock(FirebaseOptions.class));
            when(mockConfig.firebaseApp()).thenReturn(mockApp);
            return mockConfig;
        }

        @Bean
        @Primary
        public StripeService stripeService() {
            return mock(StripeService.class);
        }
    }

}

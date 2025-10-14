package com.ecommerce.praticboutic_backend_java.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirebaseConfigTest {

    private FirebaseConfig config = new FirebaseConfig();

    @AfterEach
    void tearDown() {
        // Supprime toutes les apps Firebase pour isoler les tests
        List<FirebaseApp> apps = FirebaseApp.getApps();
        for (FirebaseApp app : apps) {
            app.delete();
        }
    }

    @DisplayName("firebaseApp - initialise avec un mock FirebaseApp")
    void firebaseApp_initializesWithMock() throws Exception {
        // Mock de FirebaseApp et FirebaseOptions
        FirebaseOptions mockOptions = mock(FirebaseOptions.class);
        FirebaseApp mockApp = mock(FirebaseApp.class);
        when(mockApp.getOptions()).thenReturn(mockOptions);

        // Injection via le hook spécifique FirebaseAppSupplier (à créer dans FirebaseConfig)
        config.setFirebaseAppSupplier(() -> mockApp);

        FirebaseApp app = config.firebaseApp();

        assertNotNull(app);
        assertSame(mockApp, app);
        assertSame(mockOptions, app.getOptions());
    }


    @Test
    @DisplayName("firebaseApp - lève Exception si le flux est null")
    void firebaseApp_throwsWhenStreamNull() {
        config.setServiceAccountSupplier(() -> null);

        Exception ex = assertThrows(Exception.class, config::firebaseApp);
        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("Firebase Service Account key not found."));
    }

    @Test
    @DisplayName("firebaseMessaging - retourne l'instance liée à FirebaseApp fournie")
    void firebaseMessaging_returnsInstance() throws Exception {
        // Mock FirebaseApp
        FirebaseApp mockApp = mock(FirebaseApp.class);
        FirebaseMessaging mockMessaging = mock(FirebaseMessaging.class);

        // Mockito spy pour FirebaseMessaging (facultatif)
        config.setFirebaseAppSupplier(() -> mockApp);

        FirebaseMessaging messaging = config.firebaseMessaging(mockApp);

        assertNotNull(messaging);
        assertSame(mockMessaging, messaging);
    }
}

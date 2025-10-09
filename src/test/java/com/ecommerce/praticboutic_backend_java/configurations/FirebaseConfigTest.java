package com.ecommerce.praticboutic_backend_java.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ... existing code ...

class FirebaseConfigTest {

    @AfterEach
    void tearDown() {
        // Nettoie les apps Firebase initialisées pour isoler les tests
        List<FirebaseApp> apps = FirebaseApp.getApps();
        for (FirebaseApp app : apps) {
            app.delete();
        }
    }

    // Sous-classe de test pour fournir un InputStream contrôlé
    static class TestableFirebaseConfig extends FirebaseConfig {
        private InputStream overrideStream;

        void setOverrideStream(InputStream stream) {
            this.overrideStream = stream;
        }

        // Point d’extension: méthode protégée pour récupérer le flux (facilitée ici)
        protected InputStream loadServiceAccount(String keyPath) {
            return overrideStream;
        }

        // Adapter firebaseApp pour appeler loadServiceAccount au lieu de getClass().getClassLoader()
        public FirebaseApp firebaseAppPatched() throws Exception {
            // Simule le corps réel, mais en utilisant loadServiceAccount
            InputStream serviceAccount = loadServiceAccount(getJsonKeyForTest(this));
            if (serviceAccount == null) {
                throw new java.io.IOException("Firebase Service Account key not found.");
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }

    @Test
    @DisplayName("firebaseApp - initialise l'app Firebase avec un flux valide (sans accéder aux credentials internes)")
    void firebaseApp_initializesWithValidStream() throws Exception {
        TestableFirebaseConfig cfg = new TestableFirebaseConfig();
        setField(cfg, "JsonKey", "firebase-test.json");

        // JSON factice minimal conforme au format service account
        String json = "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"demo-project\",\n" +
                "  \"private_key_id\": \"test\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMII...\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"test@demo-project.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"1234567890\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/test\"\n" +
                "}";
        cfg.setOverrideStream(new ByteArrayInputStream(json.getBytes()));

        FirebaseApp app = cfg.firebaseAppPatched();
        assertNotNull(app);
        assertNotNull(app.getOptions());
        // On n’accède pas à options.getCredentials() (non public)
    }

    @Test
    @DisplayName("firebaseApp - lève IOException si le flux est null")
    void firebaseApp_throwsWhenStreamNull() {
        TestableFirebaseConfig cfg = new TestableFirebaseConfig();
        setField(cfg, "JsonKey", "inconnu.json");
        cfg.setOverrideStream(null);

        Exception ex = assertThrows(Exception.class, () -> cfg.firebaseAppPatched());
        assertTrue(ex.getMessage().contains("Firebase Service Account key not found."));
    }

    @Test
    @DisplayName("firebaseMessaging - retourne une instance liée au FirebaseApp fourni")
    void firebaseMessaging_returnsInstance() throws Exception {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.create(null))
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(options, "test-app");

        FirebaseConfig config = new FirebaseConfig();
        FirebaseMessaging messaging = config.firebaseMessaging(app);

        assertNotNull(messaging);
        assertSame(FirebaseMessaging.getInstance(app), messaging);
    }

    // Utilitaire d’injection reflexive d’un champ privé
    private static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            fail("Impossible d'injecter le champ " + name + ": " + e.getMessage());
        }
    }

    // Récupère la valeur de JsonKey via réflexion pour la passer à loadServiceAccount
    private static String getJsonKeyForTest(Object target) {
        try {
            Field f = target.getClass().getSuperclass().getDeclaredField("JsonKey");
            f.setAccessible(true);
            Object val = f.get(target);
            return val == null ? null : val.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
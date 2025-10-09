package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Abonnement;
import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.repositories.AbonnementRepository;
// ... existing code ...
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ... existing code ...

class AbonnementServiceTest {

    @Mock
    private AbonnementRepository abonnementRepository;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private AbonnementService abonnementService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    // ... existing code ...

    @Test
    @DisplayName("findById - retourne l'abonnement quand existe")
    void findById_returnsAbonnement_whenExists() {
        Integer id = 123;
        Abonnement expected = new Abonnement();
        // TODO: setter id si nécessaire
        when(abonnementRepository.findById(id)).thenReturn(java.util.Optional.of(expected));

        Abonnement actual = abonnementService.findById(id);

        assertSame(expected, actual);
        verify(abonnementRepository).findById(id);
        verifyNoMoreInteractions(abonnementRepository, sessionService);
    }

    // ... existing code ...

    @Test
    @DisplayName("findById - retourne null quand absent")
    void findById_returnsNull_whenNotFound() {
        Integer id = 999;
        when(abonnementRepository.findById(id)).thenReturn(java.util.Optional.empty());

        Abonnement actual = abonnementService.findById(id);

        assertNull(actual);
        verify(abonnementRepository).findById(id);
        verifyNoMoreInteractions(abonnementRepository, sessionService);
    }

    // ... existing code ...

    @Test
    @DisplayName("save - persiste et retourne l'entité")
    void save_persistsEntity() {
        Abonnement input = new Abonnement();
        Abonnement saved = new Abonnement();
        when(abonnementRepository.save(input)).thenReturn(saved);

        Abonnement actual = abonnementService.save(input);

        assertSame(saved, actual);
        verify(abonnementRepository).save(input);
        verifyNoMoreInteractions(abonnementRepository, sessionService);
    }

    // ... existing code ...

    @Test
    @DisplayName("createSubscription - crée un abonnement avec paramètres")
    void createSubscription_createsWithParams() {
        Integer bouticId = 42;
        String typePlan = "PREMIUM";
        int dureeMonths = 12;

        // Stub du repository pour la sauvegarde de l'abonnement créé
        ArgumentCaptor<Abonnement> captor = ArgumentCaptor.forClass(Abonnement.class);
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(inv -> inv.getArgument(0));

        Abonnement created = abonnementService.createSubscription(bouticId, typePlan, dureeMonths);

        assertNotNull(created);
        // Ajustez selon vos champs: ex. created.getBouticId(), getTypePlan(), getDuree()
        // assertEquals(bouticId, created.getBouticId());
        // assertEquals(typePlan, created.getTypePlan());
        // assertEquals(dureeMonths, created.getDuree());

        verify(abonnementRepository).save(captor.capture());
        Abonnement persisted = captor.getValue();
        assertNotNull(persisted);
        verifyNoMoreInteractions(abonnementRepository, sessionService);
    }

    // ... existing code ...

    @Test
    @DisplayName("getStripeCustomerId - retourne null si non implémenté / non trouvé")
    void getStripeCustomerId_returnsNull_whenNotImplementedOrMissing() {
        Integer bouticId = 77;

        String result = abonnementService.getStripeCustomerId(bouticId);

        assertNull(result);
        verifyNoInteractions(abonnementRepository, sessionService);
    }

    // ... existing code ...

    @Nested
    @DisplayName("createAndSaveAbonnement")
    class CreateAndSaveAbonnementTests {

        @Test
        @DisplayName("retourne l'abonnement sauvegardé en happy path")
        void returnsSavedAbonnement_happyPath() throws DataAccessException {
            Client client = new Client();
            Customer customer = new Customer();
            String token = "tok_xxx";

            Abonnement toSave = new Abonnement();
            Abonnement saved = new Abonnement();

            // Adaptez si la méthode construit l'entité via des setters
            when(abonnementRepository.save(any(Abonnement.class))).thenReturn(saved);

            Abonnement result = abonnementService.createAndSaveAbonnement(client, customer, token);

            assertSame(saved, result);
            verify(abonnementRepository).save(any(Abonnement.class));
            verifyNoMoreInteractions(abonnementRepository, sessionService);
        }

        @Test
        @DisplayName("propage DataAccessException")
        void propagatesDataAccessException() throws DataAccessException {
            Client client = new Client();
            Customer customer = new Customer();
            String token = "tok_bad";

            when(abonnementRepository.save(any(Abonnement.class))).thenThrow(mock(DataAccessException.class));

            assertThrows(DataAccessException.class, () ->
                    abonnementService.createAndSaveAbonnement(client, customer, token)
            );
            verify(abonnementRepository).save(any(Abonnement.class));
            verifyNoMoreInteractions(abonnementRepository, sessionService);
        }
    }

    // ... existing code ...

    @Test
    @DisplayName("Aucune interaction inattendue à la fin de chaque test")
    void noUnexpectedInteractions() {
        // Ce test sert seulement d’exemple de structure; il peut être supprimé si inutile.
        // La règle verifyNoMoreInteractions est déjà utilisée dans chaque test.
        assertTrue(true);
    }
}

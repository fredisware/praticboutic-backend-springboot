package com.example.demo.repository;

import com.ecommerce.praticboutic_backend_java.repositories.CategorieRepository;
import com.ecommerce.praticboutic_backend_java.entities.Categorie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategorieRepositoryTest {

    @Autowired
    private CategorieRepository categorieRepository;

    @Test
    @DisplayName("Doit trouver les catégories pour un customid donné ou avec catid = 0")
    void testFindByCustomidOrCatidOrderByCatid() {
        // --- ARRANGE ---
        // Création de quelques catégories
        Categorie cat1 = new Categorie();
        cat1.setCatid(0);
        cat1.setCustomid(999); // global
        cat1.setNom("Catégorie globale");

        Categorie cat2 = new Categorie();
        cat2.setCatid(1);
        cat2.setCustomid(10);
        cat2.setNom("Catégorie boutique 10");

        Categorie cat3 = new Categorie();
        cat3.setCatid(2);
        cat3.setCustomid(20);
        cat3.setNom("Catégorie boutique 20");

        categorieRepository.save(cat1);
        categorieRepository.save(cat2);
        categorieRepository.save(cat3);

        // --- ACT ---
        List<Categorie> result = categorieRepository.findByCustomidOrCatidOrderByCatid(10, 0);

        // --- ASSERT ---
        assertThat(result)
                .hasSize(2)
                .extracting(Categorie::getNom)
                .containsExactly("Catégorie globale", "Catégorie boutique 10"); // trié par catid

        assertThat(result.get(0).getCatid()).isEqualTo(0);
        assertThat(result.get(1).getCatid()).isEqualTo(1);
    }
}

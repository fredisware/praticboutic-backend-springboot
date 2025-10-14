package com.ecommerce.praticboutic_backend_java.repositories;

import com.ecommerce.praticboutic_backend_java.entities.Article;
import com.ecommerce.praticboutic_backend_java.entities.Categorie;
import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Article article1;
    private Article article2;
    private Article article3;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();

        // 🔹 Crée une catégorie valide
        Categorie cat1 = new Categorie();
        cat1.setCustomid(1);
        cat1.setNom("Catégorie 1");
        cat1.setVisible(1);
        cat1 = categorieRepository.save(cat1); // récupère l'ID généré

        Categorie cat2 = new Categorie();
        cat2.setCustomid(1);
        cat2.setNom("Catégorie 2");
        cat2.setVisible(1);
        cat2 = categorieRepository.save(cat2);

        // 🔹 Crée les articles
        article1 = new Article();
        article1.setCustomId(1);
        article1.setCatid(cat1.getCatid()); // ID valide
        article1.setVisible(1);
        article1.setNom("Article A");
        article1.setPrix(11.0);
        article1.setUnite("€");

        article2 = new Article();
        article2.setCustomId(1);
        article2.setCatid(cat2.getCatid()); // ID valide
        article2.setVisible(0);
        article2.setNom("Article B");
        article2.setPrix(12.0);
        article2.setUnite("€");

        article3 = new Article();
        article3.setCustomId(2);
        article3.setCatid(cat1.getCatid()); // ID valide
        article3.setVisible(1);
        article3.setNom("Article C");
        article3.setPrix(13.0);
        article3.setUnite("€");

        articleRepository.save(article1);
        articleRepository.save(article2);
        articleRepository.save(article3);
    }


    @Test
    void testFindByCustomID() {
        List<Article> result = articleRepository.findByCustomid(1);
        assertEquals(2, result.size());
        assertTrue(result.contains(article1));
        assertTrue(result.contains(article2));
    }

    @Test
    void testFindByCustomidAndCatid() {
        List<Article> result = articleRepository.findByCustomidAndCatid(1, 10);
        assertEquals(1, result.size());
        assertEquals(article1, result.get(0));
    }

    @Test
    void testFindByCustomidAndVisible() {
        List<Article> result = articleRepository.findByCustomidAndVisible(1, 1);
        assertEquals(1, result.size());
        assertEquals(article1, result.get(0));

        result = articleRepository.findByCustomidAndVisible(1, 0);
        assertEquals(1, result.size());
        assertEquals(article2, result.get(0));
    }

    @Test
    void testFindByCustomidNoResults() {
        List<Article> result = articleRepository.findByCustomid(999);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveArticle() {

        Client client = new Client();
        client.setNom("Client Test 3");
        clientRepository.save(client);

        Customer customer = new Customer();
        customer.setNom("Customer 3");
        customer.setCltid(client.getCltId());
        customerRepository.save(customer);

        Categorie cat = new Categorie();
        cat.setCustomid(customer.getCustomId());
        cat.setNom("Categorie test");
        categorieRepository.save(cat);

        Article article = new Article();
        article.setCustomId(customer.getCustomId());
        article.setCatid(cat.getCatid());
        article.setVisible(1);
        article.setNom("Article D");
        article.setPrix(14.0);
        article.setUnite("€");

        Article saved = articleRepository.save(article);
        assertNotNull(saved.getArtid());
        assertEquals("Article D", saved.getNom());
    }
}

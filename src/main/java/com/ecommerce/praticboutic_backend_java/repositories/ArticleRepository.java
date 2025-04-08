package com.ecommerce.praticboutic_backend_java.repositories;

import com.ecommerce.praticboutic_backend_java.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des articles
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    List<Article> findByCustomid(Integer customid);
    List<Article> findByCustomidAndCatId(Integer customid, Integer catId);
    List<Article> findByCustomidAndVisible(Integer customid, boolean visible);

}
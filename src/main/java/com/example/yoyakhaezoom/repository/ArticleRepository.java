package com.example.yoyakhaezoom.repository;

import com.example.yoyakhaezoom.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByOriginalUrl(String originalUrl);

    List<Article> findAllByOrderByCreatedAtDesc();
}
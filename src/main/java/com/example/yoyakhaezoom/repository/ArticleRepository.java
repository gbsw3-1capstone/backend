package com.example.yoyakhaezoom.repository;

import com.example.yoyakhaezoom.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByOriginalUrl(String originalUrl);

    List<Article> findAllByOrderByCreatedAtDesc();

    List<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM Article a LEFT JOIN Like l ON l.article = a GROUP BY a.id ORDER BY COUNT(l) DESC, a.createdAt DESC")
    List<Article> findTopArticlesByLikes(Pageable pageable);

    @Query("SELECT a FROM Article a LEFT JOIN Bookmark b ON b.article = a GROUP BY a.id ORDER BY COUNT(b) DESC, a.createdAt DESC")
    List<Article> findTopArticlesByBookmarks(Pageable pageable);

    @Query("SELECT a FROM Article a ORDER BY a.viewCount DESC, a.createdAt DESC")
    List<Article> findTopArticlesByViewCount(Pageable pageable);

    @Query("SELECT a FROM Article a ORDER BY a.dailyViewCount DESC, a.createdAt DESC")
    List<Article> findTopArticlesByDailyViewCount(Pageable pageable);

    @Query("SELECT a FROM Article a ORDER BY a.weeklyViewCount DESC, a.createdAt DESC")
    List<Article> findTopArticlesByWeeklyViewCount(Pageable pageable);

    @Query("SELECT max(a.id) FROM Article a")
    Optional<Long> findMaxId();
}
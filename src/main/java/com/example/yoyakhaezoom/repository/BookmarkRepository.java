package com.example.yoyakhaezoom.repository;

import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.entity.Bookmark;
import com.example.yoyakhaezoom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndArticle(User user, Article article);

    List<Bookmark> findAllByUser(User user);
}
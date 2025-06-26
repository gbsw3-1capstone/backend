package com.example.yoyakhaezoom.repository;

import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.entity.Like;
import com.example.yoyakhaezoom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndArticle(User user, Article article);

    List<Like> findAllByUser(User user);
}
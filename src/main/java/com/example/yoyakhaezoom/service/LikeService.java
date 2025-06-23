package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.entity.Like;
import com.example.yoyakhaezoom.entity.User;
import com.example.yoyakhaezoom.repository.ArticleRepository;
import com.example.yoyakhaezoom.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public String toggleLike(Long articleId, User user) {
        Article article = articleRepository.findById(articleId).orElseThrow(
                () -> new IllegalArgumentException("게시글이 존재하지 않습니다.")
        );

        Optional<Like> likeOptional = likeRepository.findByUserAndArticle(user, article);

        if (likeOptional.isPresent()) {
            likeRepository.delete(likeOptional.get());
            return "좋아요가 취소되었습니다.";
        } else {
            Like like = Like.builder().user(user).article(article).build();
            likeRepository.save(like);
            return "좋아요를 눌렀습니다.";
        }
    }
}
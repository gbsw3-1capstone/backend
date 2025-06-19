package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.dto.CommentRequestDto;
import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.entity.Comment;
import com.example.yoyakhaezoom.entity.User;
import com.example.yoyakhaezoom.repository.ArticleRepository;
import com.example.yoyakhaezoom.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public Comment createComment(Long articleId, CommentRequestDto requestDto, User user) {
        Article article = articleRepository.findById(articleId).orElseThrow(
                () -> new IllegalArgumentException("선택한 게시물은 존재하지 않습니다.")
        );

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .user(user)
                .article(article)
                .build();

        return commentRepository.save(comment);
    }
}
package com.example.yoyakhaezoom.dto;

import com.example.yoyakhaezoom.entity.Article;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
public class ArticleResponseDto {
    private Long id;
    private String title;
    private String summary;
    private String originalUrl;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> comments;

    public ArticleResponseDto(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.summary = article.getSummary();
        this.originalUrl = article.getOriginalUrl();
        this.createdAt = article.getCreatedAt();
        this.comments = article.getComments().stream()
                .map(CommentResponseDto::new)
                .sorted(Comparator.comparing(CommentResponseDto::getCreatedAt).reversed())
                .toList();
    }
}
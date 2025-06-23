package com.example.yoyakhaezoom.dto;

import com.example.yoyakhaezoom.entity.Article;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArticleResponseDto {
    private Long id;
    private String title;
    private String summary;
    private String originalUrl;
    private LocalDateTime createdAt;

    public ArticleResponseDto(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.summary = article.getSummary();
        this.originalUrl = article.getOriginalUrl();
        this.createdAt = article.getCreatedAt();
    }
}
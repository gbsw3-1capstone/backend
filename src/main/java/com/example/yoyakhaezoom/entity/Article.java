package com.example.yoyakhaezoom.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String originalUrl;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column
    private String imageUrl;

    @Column
    private String category;

    @Builder
    public Article(String originalUrl, String title, String summary, String imageUrl, String category) {
        this.originalUrl = originalUrl;
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}
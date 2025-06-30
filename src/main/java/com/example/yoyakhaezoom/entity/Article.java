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

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private long likeCount = 0;

    @Column(nullable = false)
    private long bookmarkCount = 0;

    @Column(nullable = false)
    private int dailyViewCount = 0;

    @Column(nullable = false)
    private int weeklyViewCount = 0;

    @Builder
    public Article(String originalUrl, String title, String summary, String imageUrl, String category) {
        this.originalUrl = originalUrl;
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public void increaseViewCount() {
        this.viewCount++;
        this.dailyViewCount++;
        this.weeklyViewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }

    public void resetDailyViewCount() {
        this.dailyViewCount = 0;
    }

    public void resetWeeklyViewCount() {
        this.weeklyViewCount = 0;
    }
}
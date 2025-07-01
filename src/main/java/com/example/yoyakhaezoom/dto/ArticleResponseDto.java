package com.example.yoyakhaezoom.dto;

import com.example.yoyakhaezoom.entity.Article;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class ArticleResponseDto {
    private final Long id;
    private final String title;
    private final String tag;
    private final String tag2;
    private final String small_tag;
    private final String small_tag2;
    private final String date;
    private final String summary;
    private final String summary_highlight;
    private final String image;
    private final String news_source;
    private final String news_link;
    private final boolean liked;
    private final boolean bookmarked;
    private final long likeCount;
    private final long bookmarkCount;
    private final int viewCount;
    private final int dailyViewCount;
    private final int weeklyViewCount;

    public ArticleResponseDto(Article article, boolean liked, boolean bookmarked) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.date = article.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.image = article.getImageUrl();
        this.news_link = article.getOriginalUrl();
        this.news_source = parseNewsSource(article.getOriginalUrl());

        AiSummaryDto aiSummary = parseAiSummary(article.getSummary());
        this.summary = aiSummary.getCoreSummary();
        this.summary_highlight = aiSummary.getSummaryHighlight();
        this.tag = aiSummary.getTag();
        this.tag2 = aiSummary.getTag2();
        this.small_tag = aiSummary.getSmall_tag();
        this.small_tag2 = aiSummary.getSmall_tag2();

        this.liked = liked;
        this.bookmarked = bookmarked;
        this.likeCount = article.getLikeCount();
        this.bookmarkCount = article.getBookmarkCount();
        this.viewCount = article.getViewCount();
        this.dailyViewCount = article.getDailyViewCount();
        this.weeklyViewCount = article.getWeeklyViewCount();
    }

    private AiSummaryDto parseAiSummary(String jsonSummary) {
        if (jsonSummary == null || jsonSummary.isEmpty()) {
            return new AiSummaryDto();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonSummary, AiSummaryDto.class);
        } catch (JsonProcessingException e) {
            return new AiSummaryDto();
        }
    }

    private String parseNewsSource(String url) {
        if (url != null && url.contains("yna.co.kr")) {
            return "연합뉴스";
        }
        return "기타";
    }
}

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
    private final String small_tag;
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

    public ArticleResponseDto(Article article, boolean liked, boolean bookmarked, long likeCount, long bookmarkCount) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.tag = article.getCategory();
        this.small_tag = article.getCategory();
        this.date = article.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.image = article.getImageUrl();
        this.news_link = article.getOriginalUrl();
        this.news_source = parseNewsSource(article.getOriginalUrl());

        AiSummaryDto aiSummary = parseAiSummary(article.getSummary());
        this.summary = aiSummary.getCoreSummary();
        this.summary_highlight = aiSummary.getSummaryHighlight();

        this.liked = liked;
        this.bookmarked = bookmarked;
        this.likeCount = likeCount;
        this.bookmarkCount = bookmarkCount;
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
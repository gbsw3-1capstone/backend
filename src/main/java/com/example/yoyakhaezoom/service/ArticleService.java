package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.dto.SummarizeRequestDto;
import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "ArticleService")
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Transactional
    public Article summarizeAndSaveArticle(SummarizeRequestDto requestDto) throws IOException {
        return internalSummarize(requestDto.getUrl());
    }

    @Transactional
    public Article summarizeArticleByUrl(String url) throws IOException {
        return internalSummarize(url);
    }

    private Article internalSummarize(String url) throws IOException {
        log.info("Crawling URL: {}", url);
        Document doc = Jsoup.connect(url).get();
        String title = doc.title();

        Element articleBody = doc.select("div.story-news").first();
        if (articleBody == null) {
            log.error("기사 본문을 찾을 수 없습니다. URL: {}", url);
            throw new IOException("기사 본문을 찾을 수 없습니다. (선택자: div.story-news)");
        }
        String content = articleBody.text();

        log.info("Requesting summary to OpenAI for title: {}", title);
        String summary = getSummaryFromOpenAI(title, content);

        Article article = Article.builder()
                .originalUrl(url)
                .title(title)
                .summary(summary)
                .build();

        Article savedArticle = articleRepository.save(article);
        log.info("Successfully summarized and saved article. Article ID: {}", savedArticle.getId());
        return savedArticle;
    }

    private String getSummaryFromOpenAI(String title, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String prompt = String.format(
                "너는 10대 청소년에게 어려운 뉴스를 설명해주는 친절한 IT 전문가 선배야. " +
                        "다음 기사의 제목은 '%s'이고 내용은 아래와 같아. " +
                        "이 기사 내용을 중학생도 이해할 수 있도록 전문 용어는 쉽게 풀어서 설명해주고, " +
                        "핵심 내용만 3~4문장으로 간결하게 요약해줘: \n\n%s",
                title,
                content.substring(0, Math.min(content.length(), 3500))
        );

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(message));
        body.put("max_tokens", 500);
        body.put("temperature", 0.5);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.postForObject(OPENAI_API_URL, requestEntity, Map.class);

        if (response == null || !response.containsKey("choices")) {
            throw new RuntimeException("OpenAI API로부터 유효한 응답을 받지 못했습니다.");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices.isEmpty()) {
            throw new RuntimeException("OpenAI API 응답에 'choices'가 비어있습니다.");
        }
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> responseMessage = (Map<String, Object>) firstChoice.get("message");

        return (String) responseMessage.get("content");
    }

    @Transactional(readOnly = true)
    public List<Article> getArticles() {
        return articleRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Article getArticle(Long id) {
        return articleRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("선택한 게시물은 존재하지 않습니다.")
        );
    }
}
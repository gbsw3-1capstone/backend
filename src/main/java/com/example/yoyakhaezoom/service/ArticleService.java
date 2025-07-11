package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.dto.AiSummaryDto;
import com.example.yoyakhaezoom.dto.SummarizeRequestDto;
import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.exception.CustomException;
import com.example.yoyakhaezoom.exception.ErrorCode;
import com.example.yoyakhaezoom.repository.ArticleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Optional;

@Slf4j(topic = "ArticleService")
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

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

        String suffixToRemove = " | 연합뉴스";
        if (title.endsWith(suffixToRemove)) {
            title = title.substring(0, title.length() - suffixToRemove.length());
        }

        Element articleBody = doc.select("div.story-news").first();
        if (articleBody == null) {
            log.error("기사 본문을 찾을 수 없습니다. URL: {}", url);
            throw new IOException("기사 본문을 찾을 수 없습니다. (선택자: div.story-news)");
        }
        String content = articleBody.text();

        String imageUrl = Optional.ofNullable(doc.select("meta[property=og:image]").first())
                .map(element -> element.attr("content"))
                .orElse(null);

        log.info("Requesting summary and tag to OpenAI for title: {}", title);
        String jsonSummary = getSummaryFromOpenAI(title, content);

        AiSummaryDto summaryDto = objectMapper.readValue(jsonSummary, AiSummaryDto.class);

        Article article = Article.builder()
                .originalUrl(url)
                .title(title)
                .summary(jsonSummary)
                .imageUrl(imageUrl)
                .category(summaryDto.getTag2())
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
                "[지시문]\n" +
                        "당신은 10대 청소년의 눈높이에 맞춰 최신 뉴스를 쉽고 재미있게 설명해주는 '요약해줌' 서비스의 AI 뉴스 튜터입니다.\n" +
                        "아래의 [기사 제목]과 [기사 내용]을 읽고, 반드시 다음 [출력 규칙]과 [출력 형식]에 맞춰 유효한 JSON 객체로만 응답해주세요.\n" +
                        "JSON 코드 블록이나 다른 설명 없이, 순수한 JSON 텍스트만 출력해야 합니다.\n\n" +
                        "[기사 제목]\n%s\n\n" +
                        "[기사 내용]\n%s\n\n" +
                        "[출력 규칙]\n" +
                        "1. 먼저 기사 내용을 분석하여 다음 4개의 대분류 태그 중 가장 적합한 것 하나를 한국어와 영어로 결정합니다:\n" +
                        "   - 한국어: [시사, 문화, IT, 스포츠]\n" +
                        "   - 영어: [Politics/Economy/Society/International, Culture/Entertainment, IT/Tech, Sports]\n" +
                        "2. 결정된 대분류 태그에 따라 소분류 태그를 한국어와 영어로 결정합니다.\n" +
                        "   - 대분류가 '시사'이면, 소분류는 [정치, 경제, 사회, 국제] (한국어) 및 [Politics, Economy, Society, International] (영어) 중에서 선택합니다.\n" +
                        "   - 대분류가 '문화'이면, 소분류는 [문화/생활, 연예] (한국어) 및 [Culture/Life, Entertainment] (영어) 중에서 선택합니다.\n" +
                        "   - 대분류가 'IT'이면, 소분류는 [기술, 테크] (한국어) 및 [Technology, Tech] (영어) 중에서 선택합니다.\n" +
                        "   - 대분류가 '스포츠'이면, 소분류는 '스포츠' (한국어) 및 'Sports' (영어)로 고정합니다.\n" +
                        "3. 'summaryHighlight'는 기사 전체의 핵심을 한 문장으로 매우 흥미롭게 요약합니다.\n" +
                        "4. 'coreSummary'는 3~4문장으로 핵심 내용을 작성합니다.\n\n" +
                        "[출력 형식]\n" +
                        "{\n" +
                        "  \"tag\": \"(규칙 1에서 결정된 영어 대분류 태그)\",\n" +
                        "  \"tag2\": \"(규칙 1에서 결정된 한국어 대분류 태그)\",\n" +
                        "  \"small_tag\": \"(규칙 2에서 결정된 영어 소분류 태그)\",\n" +
                        "  \"small_tag2\": \"(규칙 2에서 결정된 한국어 소분류 태그)\",\n" +
                        "  \"summaryHighlight\": \"(규칙 3에 따른 한 문장 요약)\",\n" +
                        "  \"coreSummary\": \"(규칙 4에 따른 3~4문장 요약)\"\n" +
                        "}",
                title,
                content.substring(0, Math.min(content.length(), 3500))
        );

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(message));
        body.put("max_tokens", 1500);
        body.put("temperature", 0.3);

        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");
        body.put("response_format", responseFormat);

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

    @Transactional
    public Article getArticle(Long id) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND)
        );
        article.increaseViewCount();
        return article;
    }

    @Transactional(readOnly = true)
    public List<Article> getRankedArticles(String sortBy, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        switch (sortBy.toLowerCase()) {
            case "likes":
                return articleRepository.findTopArticlesByLikes(pageable);
            case "bookmarks":
                return articleRepository.findTopArticlesByBookmarks(pageable);
            case "views":
                return articleRepository.findTopArticlesByViewCount(pageable);
            case "daily_views":
                return articleRepository.findTopArticlesByDailyViewCount(pageable);
            case "weekly_views":
                return articleRepository.findTopArticlesByWeeklyViewCount(pageable);
            case "latest":
            default:
                return articleRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Long getLatestArticleId() {
        return articleRepository.findMaxId().orElse(null);
    }
}
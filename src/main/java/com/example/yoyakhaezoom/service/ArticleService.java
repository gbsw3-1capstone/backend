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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

        String category = UriComponentsBuilder.fromUriString(url)
                .build().getQueryParams().getFirst("section");
        if (category != null && category.contains("/")) {
            category = category.split("/")[0];
        }

        log.info("Requesting summary to OpenAI for title: {}", title);
        String jsonSummary = getSummaryFromOpenAI(title, content);

        Article article = Article.builder()
                .originalUrl(url)
                .title(title)
                .summary(jsonSummary)
                .imageUrl(imageUrl)
                .category(category)
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
                        "아래의 [기사 제목]과 [기사 내용]을 읽고, 반드시 다음 [출력 형식]에 맞춰 유효한 JSON 객체로만 응답해주세요.\n" +
                        "JSON 코드 블록이나 다른 설명 없이, 순수한 JSON 텍스트만 출력해야 합니다.\n\n" +
                        "[기사 제목]\n%s\n\n" +
                        "[기사 내용]\n%s\n\n" +
                        "[출력 형식]\n" +
                        "{\n" +
                        "  \"summaryHighlight\": \"(기사 전체를 한 문장으로 매우 매력적이고 흥미롭게 요약)\",\n" +
                        "  \"coreSummary\": \"(3~4문장의 핵심 요약을 친근한 해요체로 작성)\",\n" +
                        "  \"terms\": [\n" +
                        "    { \"term\": \"(본문의 어려운 용어 1)\", \"explanation\": \"(용어1의 쉬운 설명)\" },\n" +
                        "    { \"term\": \"(본문의 어려운 용어 2)\", \"explanation\": \"(용어2의 쉬운 설명)\" }\n" +
                        "  ],\n" +
                        "  \"discussionPoint\": \"(이 뉴스와 관련해 생각해볼 만한 흥미로운 질문 1가지)\"\n" +
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
        body.put("max_tokens", 1000);
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
                () -> new IllegalArgumentException("선택한 게시물은 존재하지 않습니다.")
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
            case "latest":
            default:
                return articleRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }
}

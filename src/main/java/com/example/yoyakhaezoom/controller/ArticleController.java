package com.example.yoyakhaezoom.controller;

import com.example.yoyakhaezoom.dto.ArticleResponseDto;
import com.example.yoyakhaezoom.dto.SummarizeRequestDto;
import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.entity.User;
import com.example.yoyakhaezoom.repository.BookmarkRepository;
import com.example.yoyakhaezoom.repository.LikeRepository;
import com.example.yoyakhaezoom.security.UserDetailsImpl;
import com.example.yoyakhaezoom.service.ArticleService;
import com.example.yoyakhaezoom.service.ScheduledCrawlingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "2. 기사 API", description = "뉴스 기사 요약 및 조회 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ScheduledCrawlingService scheduledCrawlingService;

    @Operation(summary = "뉴스 기사 요약 요청", description = "뉴스 기사 원문 URL을 받아 AI 요약을 요청하고 DB에 저장합니다. (로그인 필요)")
    @PostMapping("/articles/summarize")
    public ResponseEntity<ArticleResponseDto> summarizeArticle(
            @RequestBody SummarizeRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Article summarizedArticle = articleService.summarizeAndSaveArticle(requestDto);
        return ResponseEntity.ok(new ArticleResponseDto(summarizedArticle, false, false, 0L, 0L));
    }

    @Operation(summary = "전체 요약 기사 목록 조회", description = "지금까지 요약된 모든 기사 목록을 최신순으로 조회합니다.")
    @GetMapping("/articles")
    public ResponseEntity<List<ArticleResponseDto>> getArticles(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Article> articles = articleService.getArticles();
        List<ArticleResponseDto> responseDtos = mapArticlesToResponseDtos(articles, userDetails);
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "특정 요약 기사 상세 조회", description = "ID를 이용하여 특정 요약 기사 한 개의 상세 정보(좋아요, 북마크 여부 포함)를 조회합니다.")
    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponseDto> getArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Article article = articleService.getArticle(id);
        boolean isLiked = false;
        boolean isBookmarked = false;
        if (userDetails != null) {
            User user = userDetails.getUser();
            isLiked = likeRepository.findByUserAndArticle(user, article).isPresent();
            isBookmarked = bookmarkRepository.findByUserAndArticle(user, article).isPresent();
        }
        return ResponseEntity.ok(new ArticleResponseDto(article, isLiked, isBookmarked, article.getLikeCount(), article.getBookmarkCount()));
    }

    @Operation(summary = "요약 기사 랭킹 조회", description = "정렬 기준(latest, likes, bookmarks, views)에 따라 기사 목록을 조회합니다.")
    @GetMapping("/articles/ranking")
    public ResponseEntity<List<ArticleResponseDto>> getArticleRanking(
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Article> rankedArticles = articleService.getRankedArticles(sortBy, limit);
        List<ArticleResponseDto> responseDtos = mapArticlesToResponseDtos(rankedArticles, userDetails);
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "[테스트용] 수동 크롤링 실행", description = "자동 크롤링 기능을 지금 즉시 강제로 실행합니다.")
    @PostMapping("/admin/crawling-test")
    public ResponseEntity<String> manualCrawling() {
        scheduledCrawlingService.crawlLatestNews();
        return ResponseEntity.ok("수동 크롤링 실행 완료. 서버 로그를 확인하세요.");
    }

    @Operation(summary = "[관리자용] 자동 크롤링 중단", description = "예약된 자동 크롤링 기능을 중단시킵니다.")
    @PostMapping("/admin/crawling/disable")
    public ResponseEntity<String> disableCrawling() {
        scheduledCrawlingService.disableCrawling();
        return ResponseEntity.ok("자동 크롤링이 중단되었습니다.");
    }

    @Operation(summary = "[관리자용] 자동 크롤링 재시작", description = "중단된 자동 크롤링 기능을 다시 활성화합니다.")
    @PostMapping("/admin/crawling/enable")
    public ResponseEntity<String> enableCrawling() {
        scheduledCrawlingService.enableCrawling();
        return ResponseEntity.ok("자동 크롤링이 활성화되었습니다.");
    }

    private List<ArticleResponseDto> mapArticlesToResponseDtos(List<Article> articles, UserDetailsImpl userDetails) {
        Set<Long> likedArticleIds = Collections.emptySet();
        Set<Long> bookmarkedArticleIds = Collections.emptySet();

        if (userDetails != null) {
            User user = userDetails.getUser();
            likedArticleIds = likeRepository.findAllByUser(user).stream()
                    .map(like -> like.getArticle().getId())
                    .collect(Collectors.toSet());
            bookmarkedArticleIds = bookmarkRepository.findAllByUser(user).stream()
                    .map(bookmark -> bookmark.getArticle().getId())
                    .collect(Collectors.toSet());
        }

        Set<Long> finalLikedIds = likedArticleIds;
        Set<Long> finalBookmarkedIds = bookmarkedArticleIds;

        return articles.stream()
                .map(article -> new ArticleResponseDto(
                        article,
                        finalLikedIds.contains(article.getId()),
                        finalBookmarkedIds.contains(article.getId()),
                        article.getLikeCount(),
                        article.getBookmarkCount()
                ))
                .collect(Collectors.toList());
    }
}
package com.example.yoyakhaezoom.controller;

import com.example.yoyakhaezoom.dto.ArticleResponseDto;
import com.example.yoyakhaezoom.dto.SummarizeRequestDto;
import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.security.UserDetailsImpl;
import com.example.yoyakhaezoom.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "2. 기사 API", description = "뉴스 기사 요약 및 조회 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "뉴스 기사 요약 요청", description = "뉴스 기사 원문 URL을 받아 AI 요약을 요청하고 DB에 저장합니다. (로그인 필요)")
    @PostMapping("/articles/summarize")
    public ResponseEntity<ArticleResponseDto> summarizeArticle(
            @RequestBody SummarizeRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Article summarizedArticle = articleService.summarizeAndSaveArticle(requestDto);
        return ResponseEntity.ok(new ArticleResponseDto(summarizedArticle));
    }

    @Operation(summary = "전체 요약 기사 목록 조회", description = "지금까지 요약된 모든 기사 목록을 최신순으로 조회합니다.")
    @GetMapping("/articles")
    public ResponseEntity<List<ArticleResponseDto>> getArticles() {
        List<ArticleResponseDto> articles = articleService.getArticles().stream()
                .map(ArticleResponseDto::new)
                .toList();
        return ResponseEntity.ok(articles);
    }

    @Operation(summary = "특정 요약 기사 상세 조회", description = "ID를 이용하여 특정 요약 기사 한 개의 상세 정보(댓글 포함)를 조회합니다.")
    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponseDto> getArticle(@PathVariable Long id) {
        Article article = articleService.getArticle(id);
        return ResponseEntity.ok(new ArticleResponseDto(article));
    }
}
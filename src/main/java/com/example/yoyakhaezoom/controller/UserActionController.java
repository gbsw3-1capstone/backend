package com.example.yoyakhaezoom.controller;

import com.example.yoyakhaezoom.security.UserDetailsImpl;
import com.example.yoyakhaezoom.service.BookmarkService;
import com.example.yoyakhaezoom.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3. 사용자 활동 API", description = "좋아요, 북마크 처리 (로그인 필요)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/{articleId}")
public class UserActionController {

    private final LikeService likeService;
    private final BookmarkService bookmarkService;

    @Operation(summary = "게시글 좋아요/취소 (토글)", description = "특정 게시글에 대한 '좋아요'를 추가하거나 취소합니다.")
    @PostMapping("/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String message = likeService.toggleLike(articleId, userDetails.getUser());
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "게시글 북마크/취소 (토글)", description = "특정 게시글에 대한 '북마크'를 추가하거나 취소합니다.")
    @PostMapping("/bookmark")
    public ResponseEntity<String> toggleBookmark(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String message = bookmarkService.toggleBookmark(articleId, userDetails.getUser());
        return ResponseEntity.ok(message);
    }
}
package com.example.yoyakhaezoom.controller;

import com.example.yoyakhaezoom.dto.CommentRequestDto;
import com.example.yoyakhaezoom.dto.CommentResponseDto;
import com.example.yoyakhaezoom.entity.Comment;
import com.example.yoyakhaezoom.security.UserDetailsImpl;
import com.example.yoyakhaezoom.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "3. 댓글 API", description = "기사에 대한 댓글 작성 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "특정 기사에 댓글을 작성합니다. (로그인 필요)")
    @PostMapping("/articles/{articleId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long articleId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Comment savedComment = commentService.createComment(articleId, requestDto, userDetails.getUser());
        return ResponseEntity.ok(new CommentResponseDto(savedComment));
    }
}
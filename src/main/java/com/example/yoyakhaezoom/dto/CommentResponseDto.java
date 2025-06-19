package com.example.yoyakhaezoom.dto;

import com.example.yoyakhaezoom.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private Long id;
    private String content;
    private String authorNickname;
    private LocalDateTime createdAt;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorNickname = comment.getUser().getNickname();
        this.createdAt = comment.getCreatedAt();
    }
}
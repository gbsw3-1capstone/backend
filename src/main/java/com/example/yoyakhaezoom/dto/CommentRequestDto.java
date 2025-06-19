package com.example.yoyakhaezoom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentRequestDto {
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;
}
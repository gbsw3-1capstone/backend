package com.example.yoyakhaezoom.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponseDto {
    private final int statusCode;
    private final String message;

    @Builder
    public ErrorResponseDto(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
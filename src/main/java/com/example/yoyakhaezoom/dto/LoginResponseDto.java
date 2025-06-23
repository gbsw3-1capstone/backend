package com.example.yoyakhaezoom.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String token;
    private Long userId;

    public LoginResponseDto(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }
}
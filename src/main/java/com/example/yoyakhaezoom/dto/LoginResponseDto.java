package com.example.yoyakhaezoom.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String username;
    private String nickname;

    public LoginResponseDto(String token, Long userId, String username, String nickname) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
    }
}
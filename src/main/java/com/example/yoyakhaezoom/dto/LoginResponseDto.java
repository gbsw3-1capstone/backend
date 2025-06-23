package com.example.yoyakhaezoom.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String username; // 추가
    private String nickname; // 추가

    public LoginResponseDto(String token, Long userId, String username, String nickname) { // 생성자 수정
        this.token = token;
        this.userId = userId;
        this.username = username; // 추가
        this.nickname = nickname; // 추가
    }
}
package com.example.yoyakhaezoom.controller;

import com.example.yoyakhaezoom.dto.LoginRequestDto;
import com.example.yoyakhaezoom.dto.LoginResponseDto;
import com.example.yoyakhaezoom.dto.SignupRequestDto;
import com.example.yoyakhaezoom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "1. 사용자 인증 API", description = "사용자 회원가입 및 로그인 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "사용자 ID, 비밀번호, 닉네임을 받아 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @Operation(summary = "로그인", description = "사용자 ID, 비밀번호로 로그인을 진행하고 Response Body에 JWT 토큰과 사용자 ID를 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseData = userService.login(requestDto);

        return ResponseEntity.ok(responseData);
    }
}
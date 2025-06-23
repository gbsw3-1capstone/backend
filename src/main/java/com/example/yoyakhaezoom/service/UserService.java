package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.dto.LoginRequestDto;
import com.example.yoyakhaezoom.dto.LoginResponseDto; // DTO 임포트
import com.example.yoyakhaezoom.dto.SignupRequestDto;
import com.example.yoyakhaezoom.entity.User;
import com.example.yoyakhaezoom.repository.UserRepository;
import com.example.yoyakhaezoom.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String rawPassword = requestDto.getPassword();
        String nickname = requestDto.getNickname();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(nickname)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto requestDto) { // 반환 타입을 LoginResponseDto로 변경
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createToken(user.getUsername());

        // LoginResponseDto 객체를 직접 생성하여 반환
        return new LoginResponseDto(token, user.getId(), user.getUsername(), user.getNickname());
    }
}
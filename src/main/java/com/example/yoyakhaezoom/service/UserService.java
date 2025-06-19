package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.dto.LoginRequestDto;
import com.example.yoyakhaezoom.dto.SignupRequestDto;
import com.example.yoyakhaezoom.entity.User;
import com.example.yoyakhaezoom.repository.UserRepository;
import com.example.yoyakhaezoom.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void login(LoginRequestDto requestDto, HttpServletResponse res) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createToken(user.getUsername());
        res.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
    }
}
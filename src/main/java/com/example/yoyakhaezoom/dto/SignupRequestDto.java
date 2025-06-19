package com.example.yoyakhaezoom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SignupRequestDto {

    @NotBlank(message = "사용자 ID는 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "사용자 ID는 4~10자의 영문 소문자, 숫자로 작성해주세요.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,15}$", message = "비밀번호는 8~15자의 영문, 숫자를 최소 1자 이상 포함해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;
}
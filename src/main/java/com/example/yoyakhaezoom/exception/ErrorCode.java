package com.example.yoyakhaezoom.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터 값을 확인해주세요."),
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 사용자가 없습니다."),
    DUPLICATE_ID(HttpStatus.CONFLICT, "이미 존재하는 ID입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
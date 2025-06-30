package com.example.yoyakhaezoom.exception;

import com.example.yoyakhaezoom.dto.ErrorResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .statusCode(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .statusCode(ErrorCode.INVALID_PARAMETER.getHttpStatus().value())
                .message(errorMessage)
                .build();
        return ResponseEntity.status(ErrorCode.INVALID_PARAMETER.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .statusCode(ErrorCode.INVALID_PARAMETER.getHttpStatus().value())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.INVALID_PARAMETER.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        ex.printStackTrace();
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .statusCode(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(errorResponse);
    }
}
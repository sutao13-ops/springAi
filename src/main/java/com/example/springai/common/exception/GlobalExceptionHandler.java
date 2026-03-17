package com.example.springai.common.exception;

import com.example.springai.common.ApiResponse;
import com.example.springai.common.ChatResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author SuTao
 * @Date 2026/3/17 16:46
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<ChatResponse> handleException(Exception e) {
        return ApiResponse.success(ChatResponse.error(e.getMessage()));
    }
}

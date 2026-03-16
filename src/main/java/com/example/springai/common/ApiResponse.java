package com.example.springai.common;

import java.time.Instant;
import java.util.Objects;

public record ApiResponse<T>(
        boolean success,
        int code,
        String message,
        T data,
        Instant timestamp
) {

    public ApiResponse {
        message = Objects.requireNonNullElse(message, "");
        timestamp = Objects.requireNonNullElse(timestamp, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "success", data, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, 200, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, 500, message, null, Instant.now());
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(false, code, message, null, Instant.now());
    }
}

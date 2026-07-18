package com.example.druiddemo.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(boolean success, T data, String message, LocalDateTime timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, "创建成功", LocalDateTime.now());
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, null, message, LocalDateTime.now());
    }
}
